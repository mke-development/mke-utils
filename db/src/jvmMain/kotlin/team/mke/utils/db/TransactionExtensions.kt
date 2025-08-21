package team.mke.utils.db

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.core.statements.StatementType
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import java.sql.ResultSet

fun JdbcTransaction.execSelect(
    @Language("sql") stmt: String,
    args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
    explicitStatementType: StatementType? = null,
    block: (ResultSet) -> Unit
) = exec(stmt, args, explicitStatementType) {
    it.forEach(block)
}

fun <R> JdbcTransaction.execSelectMap(
    @Language("sql") stmt: String,
    args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
    explicitStatementType: StatementType? = null,
    map: (ResultSet) -> R
): List<R> = exec(stmt, args, explicitStatementType) {
    val res = mutableListOf<R>()
    it.forEach { row ->
        res.add(map(row))
    }
    return@exec res
} ?: emptyList()