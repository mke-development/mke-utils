package team.mke.utils.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.time.format.DateTimeFormatter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObjectInstance

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

@Suppress("UNCHECKED_CAST")
inline fun <ID: Comparable<ID>, REF: Comparable<REF>, reified R : Entity<REF>, S : Entity<ID>> S.wrapRowOrDefault(
    alias: Alias<IdTable<ID>>? = null, defaultValue: S.() -> R?
): R? {
    val entityClass = R::class.companionObjectInstance as EntityClass<REF, R>

    val cols = (alias ?: entityClass.table).columns
    val b1 = readValues.hasValues(cols)

    val ex = alias?.let { it[entityClass.table.id] } ?: entityClass.table.id
    val b2 = readValues.getOrNull(ex) != null

    return if (b1 && b2) {
        if (alias != null) {
            entityClass.wrapRow(readValues, alias)
        } else {
            entityClass.wrapRow(readValues)
        }
    }
    else this.defaultValue()
}

fun ResultRow.hasValues(c: List<Column<*>>) = c.all { this.hasValue(it) }

inline fun <T> withCurrentTransaction(block: Transaction.() -> T) = with(TransactionManager.current(), block)

context(E)
inline fun <ID : Comparable<ID>, reified E : Entity<ID>, RID : Comparable<RID>, reified R : Entity<RID>> EntityClass<RID, R>.optionalReferencedOn(
    column: Column<EntityID<ID>?>,
    alias: Alias<IdTable<ID>>
): ReadWriteProperty<Any?, R?> {
    val entity = this@E

    return object : ReadWriteProperty<Any?, R?> {
        val ref = optionalReferencedOn(column)

        override fun getValue(thisRef: Any?, property: KProperty<*>): R? {
            return wrapRowOrDefault(alias) {
                ref.getValue(this, property)
            }
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: R?) {
            ref.setValue(entity, property, value)
        }
    }
}