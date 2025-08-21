package team.mke.utils.db

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import ru.raysmith.utils.endOfWord
import ru.raysmith.utils.letIf
import team.mke.utils.db.sql.exists
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter

val dbDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val dbDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun <ID : Any> ID.toEntityID(table: IdTable<ID>) = EntityID(this, table)

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

/**
 * Gets an [Entity] by its [id] value or throw [EntityNotFoundException] with [message] if entity does not exist.
 * If entity's table is [NotDeletableTable], then the entity will be not deleted.
 *
 * @param id The id value of the entity.
 * @return The entity that has this id value, or `null` if no entity was found.
 */
inline fun <ID : Any, reified T : Entity<ID>> EntityClass<ID, T>.findByIdOrThrow(id: ID, message: String? = null): T {
    val entity = if (this is NotDeletableEntityClass<ID, *>) {
        findById(id)?.letIf({ it.isDeleted() }) { null }
    } else {
        findById(id)
    }

    return entity.orThrow(id, message)
}

@JvmName("requireLengthNullable")
fun requireLength(column: Column<String?>, string: String?, error: (symbols: String) -> String) {
    if (string == null) return
    require(column.length >= string.length) {
        error(column.length.endOfWord(listOf("символ", "символа", "символов")))
    }
}

fun requireLength(column: Column<String>, string: String, error: (symbols: String) -> String) {
    require(column.length >= string.length) {
        error(column.length.endOfWord(listOf("символ", "символа", "символов")))
    }
}

fun requireLength(length: Int, string: String, error: (symbols: String) -> String) {
    require(length >= string.length) {
        error(length.endOfWord(listOf("символ", "символа", "символов")))
    }
}

fun requireLength(length: Long, string: String, error: (symbols: String) -> String) {
    require(length >= string.length) {
        error(length.endOfWord(listOf("символ", "символа", "символов")))
    }
}

context(JdbcTransaction)
fun <T> ignoreReferentialIntegrity(transaction: () -> T): T {
    exec("SET REFERENTIAL_INTEGRITY FALSE")
    val res = transaction()
    exec("SET REFERENTIAL_INTEGRITY TRUE")
    return res
}

fun <T> Collection<T>.toSizedCollection() = SizedCollection(this)
fun <ID : Any, T : Entity<ID>> T.toSizedCollection() = SizedCollection(this)
infix fun <ID: Any, T : Entity<ID>> T?.eq(other: T?) = this != null && other != null && id == other.id
infix fun <ID: Any, T : Entity<ID>> T?.neq(other: T?) = this == null || other == null || id != other.id

context(Transaction)
fun EntityClass<*, *>.any(op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
    return exists(op)
}

context(Transaction)
fun EntityClass<*, *>.none(op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
    return !exists(op)
}

context(JdbcTransaction)
fun truncate(vararg tables: Table) = truncate(tables.toList())

context(JdbcTransaction)
fun truncate(tables: List<Table>) {
    tables.forEach {
        it.truncate()
    }
}

context(JdbcTransaction)
fun Table.truncate() {
    exec("TRUNCATE TABLE $tableName")
}

val Column<BigDecimal>.scale get() = (columnType as DecimalColumnType).scale

fun BigDecimal.scaledBy(
    column: Column<BigDecimal>,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
) = setScale(column.scale, roundingMode)

fun <ID : Any, T : Entity<ID>> SizedIterable<T>.deleteAll() = forEach { it.delete() }

fun <T> ColumnSet.getFromAliasOrColumn(col: Column<T>) = if (this is Alias<*>) this[col] else col

context(Transaction, Entity<*>)
@Suppress("USELESS_CAST", "UNCHECKED_CAST")
fun <T> lookup(
    alias: Alias<IdTable<*>>?,
    col: Column<out T>, default: () -> T
): T = alias?.getFromAliasOrColumn(col)?.lookup()
    ?: writeValues[col as Column<out Any?>] as T
    ?: readValues.getOrNull(col)
    ?: default()

inline fun <ID : Any, reified T : Entity<ID>> EntityClass<ID, T>.findByIdOrNull(id: ID): T? {
    val entity = if (this is NotDeletableEntityClass<ID, *>) {
        findById(id)?.letIf({ it.isDeleted() }) { null }
    } else {
        findById(id)
    }

    return entity
}

fun <ID : Any, T : IdTable<ID>> Alias<T>?.getOrColumn(col: Column<EntityID<ID>>) = this?.get(col) ?: col

private fun defaultQuery(table: IdTable<*>): Op<Boolean> {
    if (table is NotDeletableTable<*>) {
        return table.validQueryExpression()
    }
    return Op.TRUE
}

context(EntityClass<ID, E>)
fun <ID : Any, E : Entity<ID>> getAllImpl(
    defaultQuery: Op<Boolean> = defaultQuery(table),
    onQuery: Query.() -> Unit = { },
    query: (SqlExpressionBuilder.() -> Op<Boolean>)? = null
): SizedIterable<E> {
    return table
        .selectAll()
        .where { defaultQuery.letIf(query != null) { it and query!!() } }
        .orderBy(table.id to SortOrder.DESC)
        .also { it.onQuery() }
        .mapLazy { wrapRow(it) }
}
