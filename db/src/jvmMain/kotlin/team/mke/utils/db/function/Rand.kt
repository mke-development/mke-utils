package team.mke.utils.db.function

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.QueryBuilder

class Rand : Expression<Any>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("RAND()")
    }
}