package team.mke.utils.db

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.EntityClass

/**
 * Исключение, выбрасываемое при попытке найти сущность по id, которая не существует в БД
 *
 * @param entity класс сущности, которая не была найдена
 * @param id id сущности, которая не была найдена
 * @param message сообщение исключения (необязательно, по умолчанию будет сгенерировано на основе класса сущности и id)
 * */
class EntityNotFoundException(
    val entity: EntityClass<*, *>, val id: Any? = null, message: String? = null,
) : RuntimeException(message) {

    constructor(entity: EntityClass<*, *>, id: EntityID<*>? = null, message: String? = null) : this(
        entity,
        id?.value,
        message
    )
}
