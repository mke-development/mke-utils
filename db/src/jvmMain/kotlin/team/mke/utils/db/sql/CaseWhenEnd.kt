package team.mke.utils.db.sql

import org.jetbrains.exposed.sql.*

class CaseWhenEnd<T>(
    /** The conditions to check and their results if met. */
    val caseWhen: CaseWhen<T>
) : ExpressionWithColumnType<T>(), ComplexExpression {

    override val columnType: IColumnType<T & Any> =
        caseWhen.cases.map { it.second }.filterIsInstance<ExpressionWithColumnType<T>>().firstOrNull()?.columnType
            ?: error("No column type has been found")

    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder {
            append("CASE")
            if (caseWhen.value != null) {
                +" "
                +caseWhen.value!!
            }

            for ((first, second) in caseWhen.cases) {
                append(" WHEN ", first, " THEN ", second)
            }

            append(" END")
        }
    }
}

fun <T> CaseWhen<T>.End() = CaseWhenEnd(this)