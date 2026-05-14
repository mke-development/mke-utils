package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.dao.id.IdTable

fun defaultQuery(table: IdTable<*>): Op<Boolean>? {
    if (table is NotDeletableTable<*>) {
        return table.validQueryExpression()
    }
    return null
}
