package team.mke.utils.db.function

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.QueryBuilder

/**
 * Класс для представления SQL функции `RAND`
 * */
class Rand : Expression<Any>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("RAND()")
    }
}
