package team.mke.utils.db.function

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.QueryBuilder
import team.mke.utils.db.TemporalUnits
import java.time.LocalDate
import java.time.temporal.Temporal

/**
 * Класс для представления SQL функции `DATE_ADD`
 *
 * Пример:
 * ```kotlin
 * val dateAddExpression = DateAdd(UsersAuthSessions.dateCreate, 1, TemporalUnits.DAY)
 * ```
 * Это выражение будет соответствовать SQL-запросу:
 * ```sql
 * DATE_ADD(date_create, INTERVAL 1 DAY)
 * ```
 *
 * @param date Выражение, представляющее дату, к которой будет добавлен интервал.
 * @param interval Выражение, представляющее интервал, который будет добавлен к дате.
 * @param unit Единица измерения интервала.
 * */
class DateAdd<T : Temporal?>(
    val date: Expression<T>, val interval: Expression<*>, val unit: TemporalUnits
) : Expression<LocalDate>() {
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
