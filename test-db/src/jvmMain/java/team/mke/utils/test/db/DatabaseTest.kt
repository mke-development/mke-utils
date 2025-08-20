package team.mke.utils.test.db

import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.parents
import io.kotest.extensions.system.withSystemProperties
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import team.mke.utils.db.ignoreReferentialIntegrity
import team.mke.utils.db.truncate
import team.mke.utils.io.disabledOutputStream
import team.mke.utils.io.originalOut
import java.util.UUID
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions

abstract class DatabaseTest(
    val databaseKClass: KClass<*>,
    val tables: List<Table>,
    val sqlLogger: SqlLogger = StdOutSqlLogger,
    body: DatabaseTest.() -> Unit = {}
) : FreeSpec() {

    private var connection: Database? = null

    constructor(databaseKClass: KClass<*>, tables: List<Table>, body: DatabaseTest.() -> Unit = {}) :
            this(databaseKClass, tables, StdOutSqlLogger, body)

    val usedIds = mutableMapOf<IdTable<*>, MutableList<Any>>()

    private var afterDatabaseCleared: DatabaseTest.() -> Unit = {}
    fun afterDatabaseCleared(block: DatabaseTest.() -> Unit) {
        afterDatabaseCleared = block
    }

    init {
        fun IColumnType<*>.rawSqlType(): String = when (this) {
            is AutoIncColumnType -> delegate
            is EntityIDColumnType<*> if idColumn.columnType is AutoIncColumnType ->
                (idColumn.columnType as AutoIncColumnType).delegate
            else -> this
        }.toString()

        @Suppress("UNCHECKED_CAST")
        tables.filterIsInstance<IdTable<*>>().forEach {
            it.id.defaultValueFun = {
                var nextId: Any

                while(true) {
                    nextId = when(it.id.columnType.rawSqlType()) {
                        "INT" -> {
                            val lastId = usedIds[it]?.last() as Int? ?: 0
                            Random.nextInt(lastId, lastId.plus(1000))
                        }
                        "LONG" -> {
                            val lastId = usedIds[it]?.last() as Long? ?: 0L
                            Random.nextLong(lastId, lastId.plus(1000))
                        }
                        else -> error("Unsupported IdTable type: ${it::class.simpleName}")
                    }

                    if (usedIds[it]?.contains(nextId) != true ) {
                        usedIds.getOrPut(it) { mutableListOf() }
                        usedIds[it]!!.add(nextId)
                        break
                    }
                }

                when(it.id.columnType.rawSqlType()) {
                    "INT" -> EntityID(nextId as Int, it as IdTable<Int>)
                    "LONG" -> EntityID(nextId as Long, it as IdTable<Long>)
                    else -> error("Unsupported IdTable type: ${it::class.simpleName}")
                }
            } as (() -> Nothing)?
        }

        afterTest { (test, result) ->
            System.setOut(disabledOutputStream)
            if (test.parents().none { it.config?.tags?.contains(preserveDatabaseTag) == true }) {
                transaction {
                    ignoreReferentialIntegrity {
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
                    .first { it.name == "setupEagerlyCollector" }
                    .call(databaseKClass.objectInstance)
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