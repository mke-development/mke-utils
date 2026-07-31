package team.mke.utils.db.sql

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.jdbc.Query

/**
 * Класс для представления подзапросов в SQL. Позволяет использовать результаты одного запроса в другом запросе.
 *
 * @see subQuery
 * @see [Query.asSubQuery]
 * */
class SubQueryExpression<T>(val query: Query) : Expression<T>() {
    override fun toQueryBuilder(queryBuilder: org.jetbrains.exposed.v1.core.QueryBuilder) = queryBuilder {
        append("(")
        query.prepareSQL(this)
        append(")")
    }
}

/**
 * Функция для создания подзапроса из лямбды, которая возвращает [Query].
 * Позволяет использовать результаты одного запроса в другом запросе.
 * */
fun <T> subQuery(q: () -> Query) = SubQueryExpression<T>(q())

/**
 * Функция-расширение для [Query], которая превращает его в подзапрос.
 * */
fun <T> Query.asSubQuery() = SubQueryExpression<T>(this)
