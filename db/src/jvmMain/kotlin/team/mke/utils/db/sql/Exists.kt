package team.mke.utils.db.sql

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.select

/**
 * Проверяет, существуют ли записи в таблице, удовлетворяющие условию [op].
 * @param op Условие для проверки существования записей
 * @return `true`, если существуют записи, удовлетворяющие условию, иначе `false`
 * */
fun Table.exist(op: () -> Op<Boolean>): Boolean {
    val exists = exists(select(intLiteral(1)).where(op))
    return Query(Select(this, listOf(exists)), null).first()[exists]
}

/**
 * Проверяет, существуют ли записи в таблице, связанной с [EntityClass], удовлетворяющие условию [op].
 * @param op Условие для проверки существования записей
 * @return `true`, если существуют записи, удовлетворяющие условию, иначе `false
 * */
fun EntityClass<*, *>.exists(op: () -> Op<Boolean>): Boolean {
    return table.exist(op)
}
