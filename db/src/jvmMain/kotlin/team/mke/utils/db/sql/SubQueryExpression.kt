package team.mke.utils.db.sql

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.jdbc.Query

class SubQueryExpression<T>(val query: Query) : Expression<T>() {
    override fun toQueryBuilder(queryBuilder: org.jetbrains.exposed.v1.core.QueryBuilder) = queryBuilder {
        append("(")
        query.prepareSQL(this)
        append(")")
    }
}

fun <T> subQuery(q: () -> Query) = SubQueryExpression<T>(q())
fun <T> Query.asSubQuery() = SubQueryExpression<T>(this)
