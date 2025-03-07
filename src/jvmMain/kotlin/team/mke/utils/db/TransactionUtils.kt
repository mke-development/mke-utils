package team.mke.utils.db

import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.coroutines.coroutineContext

@Deprecated("Use newSuspendedTransaction instead", ReplaceWith("newSuspendedTransaction(db, statement)", "org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction"))
/** Использует [transaction] метод, внутри [runBlocking] корутины */
inline fun <T> suspendTransaction(db: Database? = null, crossinline statement: suspend Transaction.() -> T): T {
    return transaction(db) {
        runBlocking {
            statement()
        }
    }
}

/**
 * Использует контекст корутины из [RoutingContext] для [newSuspendedTransaction]
 * */
suspend fun <T> RoutingContext.newSuspendedTransaction(
    db: Database? = null, transactionIsolation: Int? = null, readOnly: Boolean? = null,
    statement: suspend Transaction.() -> T
): T = newSuspendedTransaction(coroutineContext, db, transactionIsolation, readOnly, statement)