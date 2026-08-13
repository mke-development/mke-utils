package team.mke.utils.test.db

import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.parents
import org.jetbrains.exposed.v1.core.AutoIncColumnType
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.EntityIDColumnType
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.mariadb.MariaDBContainer
import ru.raysmith.exposedoption.Options
import team.mke.utils.db.BaseDatabase
import team.mke.utils.db.ignoreReferentialIntegrityMariaDB
import team.mke.utils.db.truncate
import team.mke.utils.io.disabledOutputStream
import team.mke.utils.io.originalOut
import java.sql.DriverManager
import java.util.UUID

abstract class DatabaseTest<T : DatabaseTest<T>>(
    val database: BaseDatabase,
    val tables: List<Table>,
    val sqlLogger: SqlLogger = StdOutSqlLogger,
    body: T.() -> Unit = {}
) : FreeSpec() {

    private var connection: Database? = null
    var enabledStepIds = true
    val dbName = "test_" + UUID.randomUUID().toString().replace("-", "")

    constructor(database: BaseDatabase, tables: List<Table>, body: T.() -> Unit = {}) :
            this(database, tables, StdOutSqlLogger, body)

    val usedIds = mutableMapOf<IdTable<*>, MutableList<Any>>()

    private var onConnect: context(Transaction) () -> Unit = {}

    private var afterDatabaseCleared: T.() -> Unit = {}
    fun afterDatabaseCleared(block: T.() -> Unit) {
        afterDatabaseCleared = block
    }

    private var ignoreReferentialIntegrityFunction: JdbcTransaction.(() -> Unit) -> Unit = { block ->
        ignoreReferentialIntegrityMariaDB {
            block()
        }
    }
    fun ignoreReferentialIntegrityFunction(f: JdbcTransaction.(() -> Unit) -> Unit) {
        ignoreReferentialIntegrityFunction = f
    }

    private var container = MariaDBContainer("mariadb:11.8.6")
        .withDatabaseName(dbName)
        .withUsername("root")
        .withPassword("")
        .withCommand(
            "--character-set-server=utf8mb4",
            "--collation-server=utf8mb4_unicode_ci",
            "--innodb-flush-log-at-trx-commit=0", // Не ждет записи лога на диск при каждом коммите
            "--innodb-doublewrite=0",              // Отключает двойную запись InnoDB
            "--sync-binlog=0",                      // Отключает флеш бинарного лога
        )
        .withTmpFs(mapOf("/var/lib/mysql" to "rw"))
        .withCreateContainerCmdModifier { cmd ->
            cmd.withName("mariadb-tests-${UUID.randomUUID()}")
        }
    fun container(container: MariaDBContainer) {
        this.container = container
    }

    init {
        fun IColumnType<*>.rawSqlType(): String = when (this) {
            is AutoIncColumnType -> delegate.sqlType()
            is EntityIDColumnType<*> if idColumn.columnType is AutoIncColumnType ->
                (idColumn.columnType as AutoIncColumnType).delegate.sqlType()
            else -> sqlType()
        }.uppercase()

        if (enabledStepIds) {
            onConnect = {
                @Suppress("UNCHECKED_CAST")
                val testableTables = tables.filterIsInstance<IdTable<*>>()
                    .toMutableList()
                    .apply {
                        remove<IdTable<*>>(Options)
                    }

                testableTables.forEach {
                    transaction {
                        val sqlType = it.id.columnType.rawSqlType()
                        if ("INT" in sqlType || "LONG" in sqlType || "BIGINT" in sqlType || "INTEGER" in sqlType) {
                            val id = (testableTables.indexOf(it) + 1) * 1_000_000
                            exec("ALTER TABLE `${it.tableName}` AUTO_INCREMENT = $id")
                            usedIds[it] = mutableListOf(id)
                        }
                    }
                }
            }
        }

        afterTest { (test, result) ->
            System.setOut(disabledOutputStream)
            if (test.parents().none { it.config?.tags?.contains(preserveDatabaseTag) == true }) {
                transaction {
                    this.ignoreReferentialIntegrityFunction {
                        tables.forEach {
                            it.truncate()
                        }
                    }
                }
                @Suppress("UNCHECKED_CAST")
                (this@DatabaseTest as T).afterDatabaseCleared()
            }
            usedIds.clear()
        }

        beforeTest { test ->
            System.setOut(originalOut)

            if (enabledStepIds && test.parents().none { it.config?.tags?.contains(preserveDatabaseTag) == true }) {
                transaction {
                    onConnect(this)
                }
            }
        }

        beforeSpec {
            container.start()

            connection = Database.connect(
                url = "jdbc:mariadb://${container.host}:${container.firstMappedPort}/${dbName}",
                driver = container.driverClassName,
                user = container.username,
                password = container.password,
                databaseConfig = DatabaseConfig {
                    sqlLogger = this@DatabaseTest.sqlLogger
                    keepLoadedReferencesOutOfTransaction = true
                }
            )

            transaction {
                SchemaUtils.create(*tables.toTypedArray())
            }

//            withSystemProperties(mapOf("DB_USER" to container.username, "DB_PASS" to container.password, "DB_NAME" to dbName)) {
                database.setupEagerlyCollector()
//            }
        }

        afterSpec {
            connection?.also {
                TransactionManager.closeAndUnregister(it)
                it.connector().close()
            }

            try {
                val systemDbUrl = "jdbc:mariadb://${container.host}:${container.firstMappedPort}/mysql"
                DriverManager.getConnection(systemDbUrl, container.username, container.password).use { conn ->
                    conn.createStatement().use { stmt ->
                        stmt.execute("DROP DATABASE IF EXISTS `$dbName`")
                    }
                }
            } catch (_: Exception) {}
        }

        @Suppress("UNCHECKED_CAST")
        (this as T).body()
    }
}
