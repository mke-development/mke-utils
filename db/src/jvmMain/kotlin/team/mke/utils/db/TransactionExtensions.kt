package team.mke.utils.db

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementType
import java.sql.ResultSet

fun Transaction.execSelect(
    @Language("sql") stmt: String,
    args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
    explicitStatementType: StatementType? = null,
    block: (ResultSet) -> Unit
) = exec(stmt, args, explicitStatementType) {
    it.forEach(block)
}

fun <R> Transaction.execSelectMap(
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