package team.mke.utils.db.function

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.QueryBuilder

class Rand : Expression<Any>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("RAND()")
    }
}