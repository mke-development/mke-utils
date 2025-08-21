package team.mke.utils.db.sql

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.select


fun Table.exist(op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
    val exists = exists(select(intLiteral(1)).where(op))
    return Query(Select(Table.Dual, listOf(exists)), null).first()[exists]
}

fun EntityClass<*, *>.exists(op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
    return table.exist(op)
}