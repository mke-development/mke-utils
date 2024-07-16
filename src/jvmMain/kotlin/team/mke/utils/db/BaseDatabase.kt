package team.mke.utils.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import ru.raysmith.exposedoption.Options
import ru.raysmith.utils.ms
import ru.raysmith.utils.outcome
import ru.raysmith.utils.properties.PropertiesFactory
import team.mke.utils.InitiableWithArgs
import team.mke.utils.Versionable
import team.mke.utils.env.Environment
import team.mke.utils.env.env
import team.mke.utils.env.envRequired
import team.mke.utils.logging.error
import java.time.ZoneId
import java.util.*
import kotlin.time.Duration.Companion.minutes

const val COLLATE_UTF8MB4_UNICODE_CI = "utf8mb4_unicode_ci"

// TODO readme
private val dbUser by envRequired("DB_USER")
private val dbPass by envRequired("DB_PASS")
private val dbName by env("DB_NAME", "fk")
private val dbHost by env("DB_HOST", "localhost")
private val dbPort by env("DB_PORT") { it.toInt() }
private val dbDriver by env("DB_DRIVER", "com.mysql.cj.jdbc.Driver")

// TODO docs; example
@Suppress("SqlNoDataSourceInspection")
abstract class BaseDatabase : InitiableWithArgs<String>(), Versionable {
    companion object {
        const val NO_MIGRATION = -1
        val logger = LoggerFactory.getLogger("database")!!
        lateinit var properties: Properties
            private set

        val timeZone by lazy { ZoneId.of(properties["serverTimezone"]?.toString() ?: "UTC") }
    }

    private val isTest = dbHost.contains(":h2")
    abstract val tables: List<Table>
    val connection: Database get() = _connection ?: error("Can't provide connection before call Database.connect()")
    private var _connection: Database? = null

    abstract fun migration(connection: Database, toVersion: Int)

    override fun init() {
        init("db.properties")
    }

    override fun init(data: String) {
        if (isInit) return
        super.init()

        connect(data)
    }

    private var config: DatabaseConfig.Builder.() -> Unit = {}
    fun config(setup: DatabaseConfig.Builder.() -> Unit) {
        config = setup
    }

    private var hikari: HikariConfig.() -> Unit = {}
    fun hikari(setup: HikariConfig.() -> Unit) {
        hikari = setup
    }

    fun connect(dbProperties: String = "db.properties") {
        try {
            _connection = initConnection(dbProperties).also {
                transaction {
                    onConnection(isTest)
                }
            }
        } catch (e: Exception) {
            logger.error(e)
            throw e
        }
    }

    /** Called after creation connection */
    open fun onConnection(isTest: Boolean) {}
    open fun beforeCreateTables() {}

    fun createMissingTablesAndColumns(vararg tables: Table = this.tables.toTypedArray()) {
        SchemaUtils.createMissingTablesAndColumns(*tables, withLogs = false)
    }
    fun addMissingColumnsStatements(vararg tables: Table = this.tables.toTypedArray()): List<String> {
        return SchemaUtils.addMissingColumnsStatements(*tables, withLogs = false)
    }

    protected fun initConnection(dbProperties: String = "db.properties"): Database {
        Database.connect(hikari(dbProperties, useDatabase = false)).also {
            transaction {
                SchemaUtils.createDatabase(dbName)
            }
            TransactionManager.closeAndUnregister(it)
        }

        val config = DatabaseConfig {
            if (Environment.isDev()) {
                defaultMaxAttempts = 1
            }
            config()
        }

        return Database.connect(hikari(dbProperties), databaseConfig = config).also {
            transaction(it) {
                val loggerInterceptor = addLogger(object : SqlLogger {
                    override fun log(context: StatementContext, transaction: Transaction) {
                        if (logger.isDebugEnabled) {
                            logger.debug(context.expandArgs(TransactionManager.current()))
                        }
                    }
                })
                beforeCreateTables()
                SchemaUtils.create(Options)

                var databaseVersion = exec("SELECT * FROM `options` WHERE `key` = 'VERSION'") { rs ->
                    if (rs.next()) rs.getString(2).toInt() else run {
                        exec("INSERT INTO `options` (`key`, `value`) VALUES ('VERSION', '1')")
                        version
                    }
                }!!

                while(databaseVersion < version && version != NO_MIGRATION) {
                    exposedLogger.info("Start migration from $databaseVersion to ${databaseVersion + 1}...")
                    migration(it, databaseVersion + 1)
                    exec("UPDATE `options` SET `value` = '${++databaseVersion}' WHERE `key` = 'VERSION'")
                }

                unregisterInterceptor(loggerInterceptor)

                createMissingTablesAndColumns()
                addMissingColumnsStatements()
                SchemaUtils.checkExcessiveIndices(*tables.toTypedArray(), withLogs = true)
                SchemaUtils.checkExcessiveForeignKeyConstraints(*tables.toTypedArray(), withLogs = true)
                registerInterceptor(loggerInterceptor)
            }
        }
    }

    private fun hikari(dbProperties: String, useDatabase: Boolean = true): HikariDataSource {
        properties = PropertiesFactory.from(dbProperties)

        val baseUrl = dbHost + (dbPort?.let { ":$it" } ?: "")
        val jdbc = if (isTest) {
            dbHost
        } else {
            val builder = StringBuilder("jdbc:mysql://$baseUrl${useDatabase.outcome("/$dbName", "")}?")

            properties.forEach { (key, value) ->
                builder.append("$key=$value&")
            }
            builder.dropLast(1).toString()
        }

        val config = HikariConfig().apply {
            driverClassName = dbDriver
            jdbcUrl = jdbc
            maximumPoolSize = Runtime.getRuntime().availableProcessors()
            username = dbUser
            password = dbPass
            validationTimeout = 1.minutes.ms

            PropertiesFactory.from("hikari.properties").forEach { key, value ->
                addDataSourceProperty(key as String, value)
            }

            hikari()
            validate()
        }
        return HikariDataSource(config)
    }
}

