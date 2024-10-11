package team.mke.utils.db.function

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.QueryBuilder
import team.mke.utils.db.TemporalUnits
import java.time.LocalDate
import java.time.temporal.Temporal

class DateAdd<T : Temporal?>(val date: Expression<T>, val interval: Expression<*>, val unit: TemporalUnits) : Expression<LocalDate>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("DATE_ADD(")
        date.toQueryBuilder(queryBuilder)
        queryBuilder.append(",INTERVAL ")
        interval.toQueryBuilder(queryBuilder)
        queryBuilder.append(" ")
        queryBuilder.append(unit.name)
        queryBuilder.append(") ")
    }
}