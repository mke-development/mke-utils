package team.mke.utils.db.function

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.QueryBuilder

fun <T : Any> Expression<T>.distinct(columnType: IColumnType<T>): Function<T> = Distinct(this, columnType)

class Distinct<T : Any>(val expr: Expression<T>, columnType: IColumnType<T>) : Function<T>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("DISTINCT (")
        expr.toQueryBuilder(queryBuilder)
        queryBuilder.append(") ")
    }
}