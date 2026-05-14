package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.LiteralOp
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.decimalLiteral
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.core.longLiteral
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import team.mke.utils.ktor.server.pagination.PaginationData
import team.mke.utils.ktor.server.pagination.Sort
import java.math.BigDecimal

fun Sort.toSortOrder() = when (this) {
    Sort.ASC -> SortOrder.ASC
    Sort.DESC -> SortOrder.DESC
}

fun <T> SizedIterable<T>.orderBy(vararg order: Pair<Expression<*>, Sort>): SizedIterable<T> =
    orderBy(*order.map { it.first to it.second.toSortOrder() }.toTypedArray())

fun Query.orderBy(vararg order: Pair<Expression<*>, Sort>): Query =
    orderBy(*order.map { it.first to it.second.toSortOrder() }.toTypedArray())

fun decimalLiteralOrNull(value: String?): LiteralOp<BigDecimal>? = value?.let { decimalLiteral(it.toBigDecimal()) }
fun longLiteralOrNull(value: String?): LiteralOp<Long>? = value?.let { longLiteral(it.toLong()) }
fun intLiteralOrNull(value: String?): LiteralOp<Int>? = value?.let { intLiteral(it.toInt()) }

sealed interface SortCol<T : Comparable<T>?> {
    val column: Column<*>

    data class Id<T : Comparable<T>>(val col: Column<EntityID<T>>) : SortCol<T> {
        override val column: Column<*> get() = col
    }

    data class Col<T : Comparable<T>?>(val col: Column<T>) : SortCol<T> {
        override val column: Column<*> get() = col
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> literalFactory(value: String?): LiteralOp<T>? = when(T::class) {
    Int::class -> intLiteralOrNull(value)
    BigDecimal::class -> decimalLiteralOrNull(value)
    Long::class -> longLiteralOrNull(value)
    else -> throw NotImplementedError("Literal factory for type ${T::class} is not implemented")
} as LiteralOp<T>?

@JvmName("sortEntityId")
inline infix fun <reified T : Comparable<T>> Column<EntityID<T>>.sort(
    paginationData: PaginationData<*>
): Pair<SortCol.Id<T>, LiteralOp<T>?> {
    return SortCol.Id(this) to literalFactory<T>(paginationData.lastSortedValue)
}

inline infix fun <reified T : Comparable<T>?> Column<T>.sort(
    paginationData: PaginationData<*>
): Pair<SortCol.Col<T>, LiteralOp<T>?> {
    return SortCol.Col(this) to literalFactory<T>(paginationData.lastSortedValue)
}

fun Query.orderBySort(
    sortColExpression: Expression<*>?,
    paginationData: PaginationData<*>?,
    idColumn: Expression<*>,
    idColumnDefaultSort: Sort
): Query {
    return if (sortColExpression != null) {
        orderBy(sortColExpression to (paginationData?.sort ?: Sort.DESC), idColumn to idColumnDefaultSort)
    } else {
        orderBy(idColumn to idColumnDefaultSort)
    }
}

