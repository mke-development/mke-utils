package team.mke.utils.test.db

import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.parents
import io.kotest.extensions.system.withSystemProperties
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
import ru.raysmith.exposedoption.Options
import team.mke.utils.db.BaseDatabase
import team.mke.utils.db.ignoreReferentialIntegrityMySQL
import team.mke.utils.db.truncate
import team.mke.utils.io.disabledOutputStream
import team.mke.utils.io.originalOut
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions

abstract class DatabaseTest(
    val databaseKClass: KClass<*>,
    val tables: List<Table>,
    val sqlLogger: SqlLogger = StdOutSqlLogger,
    body: DatabaseTest.() -> Unit = {}
) : FreeSpec() {

    private var connection: Database? = null
    var enabledStepIds = true

    constructor(databaseKClass: KClass<*>, tables: List<Table>, body: DatabaseTest.() -> Unit = {}) :
            this(databaseKClass, tables, StdOutSqlLogger, body)

    val usedIds = mutableMapOf<IdTable<*>, MutableList<Any>>()

    private var onConnect: context(Transaction) () -> Unit = {}

    private var afterDatabaseCleared: DatabaseTest.() -> Unit = {}
    fun afterDatabaseCleared(block: DatabaseTest.() -> Unit) {
        afterDatabaseCleared = block
    }

    private var ignoreReferentialIntegrityFunction: context(JdbcTransaction) (block: () -> Unit) -> Unit = { block: () -> Unit ->
        ignoreReferentialIntegrityMySQL {
            block()
        }
    }
    fun ignoreReferentialIntegrityFunction(f: context(JdbcTransaction) (block: () -> Unit) -> Unit) {
        ignoreReferentialIntegrityFunction = f
    }


    init {
        fun IColumnType<*>.rawSqlType(): String = when (this) {
            is AutoIncColumnType -> delegate
            is EntityIDColumnType<*> if idColumn.columnType is AutoIncColumnType ->
                (idColumn.columnType as AutoIncColumnType).delegate
            else -> this
        }.toString()

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
                        when(it.id.columnType.rawSqlType()) {
                            "INT", "LONG" -> {
                                val id = (testableTables.indexOf(it) + 1) * 1_000_000
                                exec("ALTER TABLE ${it.tableName} ALTER COLUMN ${it.id.name} RESTART WITH $id")
                                usedIds[it] = mutableListOf(id)
                            }
                            else -> error("Unsupported IdTable type: ${it::class.simpleName}")
                        }
                    }
                }
            }
        }

        afterTest { (test, result) ->
            System.setOut(disabledOutputStream)
            if (test.parents().none { it.config?.tags?.contains(preserveDatabaseTag) == true }) {
                transaction {
                    ignoreReferentialIntegrityFunction {
                        SchemaUtils.listTables().forEach {
                            Table(it).truncate()
                        }
                    }
                }
                afterDatabaseCleared()
            }
            usedIds.clear()
        }

        beforeTest {
            System.setOut(originalOut)
        }

        beforeSpec {
            connection = Database.connect(
                url = "jdbc:h2:mem:${UUID.randomUUID()};DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false;IGNORECASE=true",
                driver = "org.h2.Driver",
                user = "root",
                password = "",
                databaseConfig = DatabaseConfig {
                    sqlLogger = this@DatabaseTest.sqlLogger
                    keepLoadedReferencesOutOfTransaction = true
                }
            )

            transaction {
                SchemaUtils.create(*tables.toTypedArray())
            }

            // call setupEagerlyCollector function
            withSystemProperties(mapOf("DB_USER" to "root", "DB_PASS" to "", "DB_NAME" to "test")) {
                databaseKClass
                    .declaredFunctions
                    .firstOrNull { it.name == BaseDatabase::setupEagerlyCollector.name }
                    ?.call(databaseKClass.objectInstance)
            }
        }

        @Suppress("LeakingThis")
        (afterSpec {
            connection?.also {
                TransactionManager.closeAndUnregister(it)
                it.connector().close()
            }
        })
        body()
    }
}
