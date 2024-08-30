package team.mke.utils.db

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

/** Использует [transaction] метод, внутри [runBlocking] корутины */
inline fun <T> suspendTransaction(db: Database? = null, crossinline statement: suspend Transaction.() -> T): T =
    transaction(db) {
        runBlocking {
            statement()
        }
    }