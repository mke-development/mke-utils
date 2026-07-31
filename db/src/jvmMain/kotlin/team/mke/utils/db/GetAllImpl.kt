package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.EntityIDColumnType
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.mapLazy
import org.jetbrains.exposed.v1.jdbc.selectAll
import team.mke.utils.ktor.server.pagination.PaginationData
import kotlin.reflect.full.companionObjectInstance

val IdTable<*>.defaultSort get() = id to SortOrder.DESC

context(ec: EntityClass<*, *>)
fun <T> SizedIterable<T>.withDefaultOrderBy() = orderBy(ec.table.defaultSort)

inline fun <reified T : Entity<*>> SizedIterable<T>.withDefaultOrderBy() =
    orderBy((T::class.companionObjectInstance as EntityClass<*, *>).table.defaultSort)

context(ec: EntityClass<ID, E>)
fun <ID : Any, E : Entity<ID>> getAllImpl(
    defaultQuery: Op<Boolean>? = defaultQuery(ec.table),
    onQuery: Query.() -> Unit = { },
    query: (() -> Op<Boolean>?)? = null
): SizedIterable<E> {
    return ec.table
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
        .also { it.onQuery() }
        .mapLazy { ec.wrapRow(it) }
}
