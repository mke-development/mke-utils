package team.mke.utils.db.function
import org.jetbrains.exposed.v1.core.Function
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.core.QueryBuilder

fun <T : Any> Expression<T>.distinct(columnType: IColumnType<T>): Function<T> = Distinct(this, columnType)

class Distinct<T : Any>(val expr: Expression<T>, columnType: IColumnType<T>) : Function<T>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("DISTINCT (")
        expr.toQueryBuilder(queryBuilder)
        queryBuilder.append(") ")
    }
}