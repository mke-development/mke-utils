package team.mke.utils.ktor.server.ext.db

import io.ktor.server.routing.RoutingContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

@Deprecated(
    message = """"
        This function will be removed in future releases.

        Replace with `suspendTransaction()` from exposed-r2dbc instead to use a suspending transaction.

        Please leave a comment on [YouTrack](https://youtrack.jetbrains.com/issue/EXPOSED-74/Add-R2DBC-Support)
        with a use case if you believe this method should remain available for JDBC connections.
    """,
    level = DeprecationLevel.WARNING
)
/**
 * Использует контекст корутины из [RoutingContext] для [newSuspendedTransaction]
 * */
suspend fun <T> RoutingContext.newSuspendedTransaction(
    db: Database? = null, transactionIsolation: Int? = null, readOnly: Boolean? = null,
    statement: suspend JdbcTransaction.() -> T
): T = newSuspendedTransaction(call.coroutineContext, db, transactionIsolation, readOnly, statement)