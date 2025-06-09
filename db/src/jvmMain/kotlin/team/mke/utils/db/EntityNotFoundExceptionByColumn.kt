package team.mke.utils.db

import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.Column

class EntityNotFoundExceptionByColumn(value: Any?, column: Column<*>, entity: EntityClass<*, *>) : Exception(
    "Entity ${entity.javaClass.enclosingClass.simpleName}, ${column.name}=$value not found in the database"
)