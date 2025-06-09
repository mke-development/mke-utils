package team.mke.utils.db.function

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.QueryBuilder

fun <T, R : Any> Expression<T>.ifNull(value: Expression<*>, columnType: IColumnType<R>): Function<R> = IfNull(this, value, columnType)
fun <T, L : Any> Expression<T>.ifNullLiteral(value: L, columnType: IColumnType<L>): Function<L> = IfNullLiteral(this, value, columnType)

class IfNull<T, R : Any>(val expr: Expression<T>, val value: Expression<*>, columnType: IColumnType<R>) : Function<R>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("IFNULL (")
        expr.toQueryBuilder(queryBuilder)
        queryBuilder.append(",")
        value.toQueryBuilder(queryBuilder)
        queryBuilder.append(") ")
    }
}
class IfNullLiteral<T, L : Any>(val expr: Expression<T>, val value: L, columnType: IColumnType<L>) : Function<L>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("IFNULL (")
        expr.toQueryBuilder(queryBuilder)
        queryBuilder.append(",")
        queryBuilder.append(value.toString())
        queryBuilder.append(") ")
    }
}