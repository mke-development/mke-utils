package team.mke.utils.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.dao.flushCache
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import org.slf4j.LoggerFactory
import ru.raysmith.exposedoption.Options
import ru.raysmith.utils.ms
import ru.raysmith.utils.nowZoned
import ru.raysmith.utils.properties.PropertiesFactory
import team.mke.utils.db.eager.Prop
import team.mke.utils.env.Environment
import team.mke.utils.env.env
import team.mke.utils.env.envRequired
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.minutes

@Deprecated("Use utf8mb4_uca1400_ai_ci instead", ReplaceWith("Collation.utf8mb4_uca1400_ai_ci"))
const val COLLATE_UTF8MB4_UNICODE_CI = "utf8mb4_unicode_ci"

private val dbUser by envRequired("DB_USER")
private val dbPass by envRequired("DB_PASS")
private val dbName by envRequired("DB_NAME")
private val dbHost by env("DB_HOST", "localhost")
private val dbPort by env("DB_PORT") { it.toInt() }
private val dbDriver by env("DB_DRIVER", "com.mysql.cj.jdbc.Driver")
private val dbSchema by env("DB_SCHEMA", "jdbc:mysql")

// TODO docs; example
@Suppress("SqlNoDataSourceInspection")
abstract class BaseDatabase {

    open val withLogs: Boolean = false
    open val createMigrationsFiles: Boolean = false
    open val withAutoMigration: Boolean = true

    var isInit = false
        private set

    protected open var version = 1

    companion object {
        const val NO_MIGRATION = -1
        val logger = LoggerFactory.getLogger("database")!!
        private val eagerCollectorCache = Collections.synchronizedMap<KClass<*>, Array<Prop>>(mutableMapOf())
        private var eagerCollectorCacheEnabled: Boolean = true

        var eagerCollector: ((dtoClass: KClass<*>) -> Array<Prop>)? = null
        internal set
    }

    private var properties: Properties? = null

    open val tables: List<Table> by lazy {
        collectAllTables(this::class.java.packageName)
    }

    val connection: Database get() = _connection ?: error("Can't provide connection before call Database.connect()")
    private var _connection: Database? = null

    context(tr: JdbcTransaction)
    abstract fun migration(connection: Database, toVersion: Int)

    fun init() = init("db.properties")

    fun init(propertiesResourceFilePath: String) {
        if (isInit) return

        return init(PropertiesFactory.from(propertiesResourceFilePath))
    }

    fun init(properties: Properties) {
        if (isInit) return

        this.properties = properties
        setupEagerlyCollector()
        connect()
    }

    private var config: DatabaseConfig.Builder.() -> Unit = {}
    fun config(setup: DatabaseConfig.Builder.() -> Unit) {
        config = setup
    }

    private var hikari: HikariConfig.() -> Unit = {}
    fun hikari(setup: HikariConfig.() -> Unit) {
        hikari = setup
    }

    fun connect() {
        try {
            _connection = initConnection().also {
                transaction {
                    onConnection()
                }
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
            throw e
        }
    }

    fun disconnect() {
        _connection?.also {
            transaction { flushCache() }
            TransactionManager.closeAndUnregister(it)
            it.connector().close()
        }
    }

    open fun close() {
        disconnect()
    }

    /** Called after creation connection */
    protected open fun onConnection() {}
    protected open fun beforeCreateTables() {}
    open fun setupEagerlyCollector() {}

    @OptIn(ExperimentalDatabaseMigrationApi::class)
    fun createMissingTablesAndColumns(vararg tables: Table = this.tables.toTypedArray()) {
        SchemaUtils.create(*tables)

        if (createMigrationsFiles) {
            File("db-migrations").mkdirs()

            val hasStatements = MigrationUtils
                .statementsRequiredForDatabaseMigration(*tables, withLogs = withLogs)
                .isNotEmpty()

            if (hasStatements) {
                MigrationUtils.generateMigrationScript(
                    *tables,
                    scriptDirectory = "db-migrations",
                    scriptName = nowZoned().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")),
                    withLogs = withLogs
                ).also {
                    transaction {
                        exec(it.readText())
                    }
                }
            }
        } else {
            MigrationUtils.statementsRequiredForDatabaseMigration(*tables, withLogs = withLogs).apply {
                if (isNotEmpty()) {
                    transaction {
                        forEach { exec(it) }
                    }
                }
            }
        }
    }
    fun addMissingColumnsStatements(vararg tables: Table = this.tables.toTypedArray()): List<String> {
        return SchemaUtils.addMissingColumnsStatements(*tables, withLogs = withLogs)
    }

    protected fun initConnection(): Database {
        Database.connect(hikari(useDatabase = false)).also {
            transaction {
                SchemaUtils.createDatabase(dbName)
            }
            TransactionManager.closeAndUnregister(it)
        }

        val config = DatabaseConfig {
            if (!Environment.isProd()) {
                defaultMaxAttempts = 1
            }
            config()
        }

        return Database.connect(hikari(), databaseConfig = config).also {
            transaction(it) {
                beforeCreateTables()
                SchemaUtils.create(Options)

                var databaseVersion = exec("SELECT * FROM `options` WHERE `key` = 'VERSION'") { rs ->
                    if (rs.next()) rs.getString(2).toInt() else run {
                        exec("INSERT INTO `options` (`key`, `value`) VALUES ('VERSION', '$version')")
                        version
                    }
                }!!

                while(databaseVersion < version && version != NO_MIGRATION) {
                    logger.info("Start migration from $databaseVersion to ${databaseVersion + 1}...")
                    transaction {
                        migration(it, databaseVersion + 1)
                        exec("UPDATE `options` SET `value` = '${++databaseVersion}' WHERE `key` = 'VERSION'")
                    }
                }

                if (withAutoMigration) {
                    createMissingTablesAndColumns()
                    addMissingColumnsStatements()
                    SchemaUtils.checkExcessiveIndices(*tables.toTypedArray(), withLogs = withLogs)
                    SchemaUtils.checkExcessiveForeignKeyConstraints(*tables.toTypedArray(), withLogs = withLogs)
                }
            }
        }
    }

    private fun hikari(useDatabase: Boolean = true): HikariDataSource {
        val baseUrl = dbHost + (dbPort?.let { ":$it" } ?: "")
        val jdbc = buildString {
            append(dbSchema).append("://").append(baseUrl)
            if (useDatabase) {
                append("/$dbName")
            }
            append("?")
            properties?.forEach { (key, value) ->
                append("$key=$value&")
            }
        }.dropLast(1)

        val config = HikariConfig().apply {
            driverClassName = dbDriver
            jdbcUrl = jdbc
            maximumPoolSize = Runtime.getRuntime().availableProcessors()
            username = dbUser
            password = dbPass
            validationTimeout = 1.minutes.ms

            PropertiesFactory.from("hikari.properties").forEach { (key, value) ->
                addDataSourceProperty(key as String, value)
            }

            hikari.invoke(this)
            validate()
        }
        return HikariDataSource(config)
    }

    fun registerEagerlyCollector(cacheEnabled: Boolean = true, collector: EagerFieldsCollector.(dtoClass: KClass<*>, entityClass: EntityClass<*, *>?) -> Array<Prop>) {
        eagerCollectorCacheEnabled = cacheEnabled
        eagerCollector = c@ { dtoClass: KClass<*> ->
            if (eagerCollectorCacheEnabled) {
                val res = eagerCollectorCache[dtoClass]
                if (res != null) {
                    return@c res
                }
            }

            object : EagerFieldsCollector {
                override fun collect(dtoClass: KClass<*>, entityClass: EntityClass<*, *>?): Array<Prop> {
                    return collector(dtoClass, entityClass)
                }
            }.collect(dtoClass)

        }
    }
}
