package team.mke.utils.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.time.format.DateTimeFormatter

val dbDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val dbDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun <ID : Comparable<ID>> ID.toEntityID(table: IdTable<ID>) = EntityID(this, table)

/** Create SQL operator from [expression]. Return [Op.TRUE] if [T] is null */
context(SqlExpressionBuilder)
fun <T> T?.expIfNotNullOrTrue(expression: (T) -> Op<Boolean>): Op<Boolean> {
    return if (this != null) expression(this)
    else Op.TRUE
}

/** Apply [expression] with [value] to *where*. Do nothing if [value] is null */
fun <T> Query.andWhereIfNotNull(value: T?, expression: Query.(T) -> Op<Boolean>): Query {
    if (value != null) andWhere { expression(value) }
    return this
}

/** Apply [expression] with [value] to where. Do nothing if [value] is null */
fun <T> Query.andHavingIfNotNull(value: T?, expression: Query.(T) -> Op<Boolean>): Query {
    if (value != null) andHaving { expression(value) }
    return this
}

/** Create SQL operator from [expression]. Return [Op.FALSE] if [T] is null */
fun <T> T?.expIfNotNullOrFalse(expression: (T) -> Op<Boolean>): Op<Boolean> {
    return if (this != null) expression(this)
    else Op.FALSE
}

fun Op<Boolean>.andIf(condition: Boolean, op: Expression<Boolean>?): Op<Boolean> =
    if (condition) andIfNotNull(op) else this

fun Op<Boolean>.andIf(condition: Boolean, op: () -> Op<Boolean>): Op<Boolean> =
    if (condition) this and op() else this

fun ResultRow.hasValues(c: List<Column<*>>) = c.all { this.hasValue(it) }

inline fun <T> withCurrentTransaction(block: Transaction.() -> T) = with(TransactionManager.current(), block)

val Column<String?>.length @JvmName("nullableLengthExt") get() = this.columnType.length(table, this)
val Column<String>.length @JvmName("lengthExt") get() = this.columnType.length(table, this)
private fun IColumnType<String>.length(table: Table, column: Column<*>) = when(this) {
    is TextColumnType -> 65535
    is VarCharColumnType -> colLength
    else -> throw UnsupportedOperationException("Unsupported type of string column `${table.tableName}`.`${column.name}`")
}