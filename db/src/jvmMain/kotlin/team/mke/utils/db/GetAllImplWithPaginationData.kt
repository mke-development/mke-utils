package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.EntityIDColumnType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.mapLazy
import org.jetbrains.exposed.v1.jdbc.selectAll
import team.mke.utils.ktor.server.pagination.PaginationData

context(entityClass: EntityClass<ID, E>)
fun <ID : Any, E : Entity<ID>, C : Column<*>, T : Comparable<T>> getAllImpl(
    paginationData: Pair<PaginationData<T>?, Pair<C, SortOrder>>? = null,
    defaultQuery: Op<Boolean>? = defaultQuery(entityClass.table),
    onQuery: Query.() -> Unit = { },
    query: (() -> Op<Boolean>?)? = null
): SizedIterable<E> {
    return entityClass.table
        .selectAll()
        .apply {
            if (defaultQuery != null) {
                adjustWhere {
                    and { defaultQuery }
                }
            }
            if (query != null) {
                val op = query()
                if (op != null) {
                    adjustWhere {
                        and { op }
                    }
                }
            }
        }
        .let {
            if (paginationData != null && paginationData.first != null) {
                val (pageData, colData) = paginationData
                val (column, sort) = colData

                @Suppress("UNCHECKED_CAST")
                val query = if (pageData?.lastEntity != null) {
                    it.adjustWhere {
                        and {
                            when (sort) {
                                SortOrder.ASC, SortOrder.ASC_NULLS_FIRST, SortOrder.ASC_NULLS_LAST -> {
                                    if (column.columnType is EntityIDColumnType<*>) {
                                        (column as Column<EntityID<T>>).greater(pageData.lastEntity!!)
                                    } else {
                                        column.greater(pageData.lastEntity!!)
                                    }
                                }

                                SortOrder.DESC, SortOrder.DESC_NULLS_FIRST, SortOrder.DESC_NULLS_LAST -> {
                                    if (column.columnType is EntityIDColumnType<*>) {
                                        (column as Column<EntityID<T>>).less(pageData.lastEntity!!)
                                    } else {
                                        column.less(pageData.lastEntity!!)
                                    }
                                }
                            }
                        }
                    }
                } else it

                query.orderBy(colData)
                    .limit(pageData!!.count)
            } else {
                it.orderBy(entityClass.table.id to SortOrder.DESC)
            }
        }
        .also { it.onQuery() }
        .mapLazy { entityClass.wrapRow(it) }
}
