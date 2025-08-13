package team.mke.utils.db

import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class EntityNotFoundException private constructor(
    override val message: String? = null
) : RuntimeException() {
    constructor(entity: EntityClass<*, *>, id: EntityID<*>? = null, message: String? = null) : this(message ?: buildString {
        append("Сущность \"").append(entity.entityName() ?: "").append("\"")
        if (id != null) {
            append(" с id $id")
        }
        append(" не найдена")
    })
}