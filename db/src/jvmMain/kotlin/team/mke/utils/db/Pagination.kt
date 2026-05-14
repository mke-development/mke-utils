package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Query
import ru.raysmith.utils.notNull
import team.mke.utils.ktor.server.pagination.PaginationData
import team.mke.utils.ktor.server.pagination.Sort

fun <ID : Comparable<ID>, T : Comparable<T>> keysetPagination(
    column: Expression<in T>?,
    lastSortValue: Expression<in T>?,
    idColumn: Expression<EntityID<ID>>,
    lastIdValue: Expression<ID>?,
    sort: Sort?,
    idColumnDefaultSort: Sort = Sort.DESC,
): Op<Boolean>? {
    if (column != null && lastSortValue != null && lastIdValue != null) {
        val compareOp = if (sort == Sort.ASC) column greater lastSortValue else column less lastSortValue
        return compareOp.or { column.eq(lastSortValue).and {
            if (idColumnDefaultSort == Sort.ASC) idColumn.greater(lastIdValue) else idColumn.less(lastIdValue)
        } }
    } else if (lastIdValue != null) {
        return if (idColumnDefaultSort == Sort.ASC) idColumn.greater(lastIdValue) else idColumn.less(lastIdValue)
    } else {
        return null
    }
}

fun <ID : Comparable<ID>, T : Comparable<T>> keysetPagination(
    paginationData: PaginationData<ID>,
    column: Expression<in T>?,
    lastSortValue: Expression<in T>?,
    idColumn: Expression<EntityID<ID>>,
    idColumnDefaultSort: Sort = Sort.DESC,
    lastIdValue: (lastEntity: ID) -> Expression<ID>,
): Op<Boolean>? = keysetPagination(
    column, lastSortValue, idColumn, paginationData.lastEntity?.let { lastIdValue(it) }, paginationData.sort,
    idColumnDefaultSort
)

fun <ID : Comparable<ID>, T : Comparable<T>> Query.andPaginationIfNotNull(
    paginationData: PaginationData<ID>?,
    sortColExpression: Expression<in T>?,
    lastSortValue: Expression<in T>?,
    idColumn: Expression<EntityID<ID>>,
    idColumnDefaultSort: Sort = if (lastSortValue notNull sortColExpression) Sort.DESC else paginationData?.sort ?: Sort.DESC,
    useHaving: Boolean = false,
    lastIdValue: (lastEntity: ID) -> Expression<ID>,
): Query {
    fun apply(data: PaginationData<ID>): Op<Boolean>? {
        return keysetPagination(
            sortColExpression,
            lastSortValue,
            idColumn,
            data.lastEntity?.let { lastIdValue(it) },
            data.sort,
            idColumnDefaultSort
        )
    }

    return if (useHaving) {
        this.andHavingIfNotNull(paginationData) { data ->
            apply(data)
        }
    } else {
        this.andWhereIfNotNull(paginationData) { data ->
            apply(data)
        }
    }.orderBySort(sortColExpression, paginationData, idColumn, idColumnDefaultSort)
}
