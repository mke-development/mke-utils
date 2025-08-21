package team.mke.utils.db

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

@Deprecated("Use newSuspendedTransaction instead", ReplaceWith("newSuspendedTransaction(db, statement)", "org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction"))
/** Использует [transaction] метод, внутри [runBlocking] корутины */
inline fun <T> suspendTransaction(db: Database? = null, crossinline statement: suspend Transaction.() -> T): T {
    return transaction(db) {
        runBlocking {
            statement()
        }
    }
}