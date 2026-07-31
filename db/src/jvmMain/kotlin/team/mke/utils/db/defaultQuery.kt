package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.dao.id.IdTable

/**
 * Возвращает SQL оператор для выборки валидных записей из таблицы (интеграция с [NotDeletableTable]) или null, если не
 * использует такую реализацию
 * */
fun defaultQuery(table: IdTable<*>): Op<Boolean>? {
    if (table is NotDeletableTable<*>) {
        return table.validQueryExpression()
    }
    return null
}
