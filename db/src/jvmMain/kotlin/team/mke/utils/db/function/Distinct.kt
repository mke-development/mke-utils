package team.mke.utils.db.function
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Function
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.core.QueryBuilder

/**
 * Расширение для класса Expression, которое позволяет использовать SQL функцию `DISTINCT` для получения уникальных
 * значений из набора данных.
 * */
fun <T : Any> Expression<T>.distinct(columnType: IColumnType<T>): Function<T> = Distinct(this, columnType)

/**
 * Расширение для класса Expression, которое позволяет использовать SQL функцию `DISTINCT` для получения уникальных
 * значений из набора данных.
 * */
fun <T : Any> Column<T>.distinct(): Function<T> = distinct(columnType)

/**
 * Класс для представления SQL функции `DISTINCT`, которая используется для получения уникальных значений из набора данных.
 * */
class Distinct<T : Any>(val expr: Expression<T>, columnType: IColumnType<T>) : Function<T>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("DISTINCT (")
        expr.toQueryBuilder(queryBuilder)
        queryBuilder.append(") ")
    }
}
