package team.mke.utils.db

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.EntityClass


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