package team.mke.utils.ktor.server.ext.db

import io.ktor.server.routing.RoutingContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Использует контекст корутины из [RoutingContext] для [newSuspendedTransaction]
 * */
suspend fun <T> RoutingContext.newSuspendedTransaction(
    db: Database? = null, transactionIsolation: Int? = null, readOnly: Boolean? = null,
    statement: suspend Transaction.() -> T
): T = newSuspendedTransaction(call.coroutineContext, db, transactionIsolation, readOnly, statement)