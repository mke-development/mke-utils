package team.mke.utils.db.eager

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.Transaction
import team.mke.utils.db.BaseDatabase
import kotlin.reflect.KClass

/**
 * Загружает связанные сущности для коллекции объектов, необходимые для создания DTO указанного типа.
 *
 * Пример использования:
 * ```kotlin
 * User.all()
 *     .withEagerly(UserDTOs.Details::class)
 *     .map { it.toDTO<UserDTOs.Details>() }
 * ```
 *
 * @param dtoClass Класс DTO, для которого нужно загрузить связанные сущности
 * @param SRCID Тип идентификатора исходной сущности
 * @param SRC Тип исходной сущности
 * @param L Тип коллекции сущностей
 *
 * @return Коллекция сущностей с предварительно загруженными связанными объектами
 * */
context(Transaction)
@Suppress("UNCHECKED_CAST")
fun <SRCID : Any, SRC : Entity<SRCID>, L : Iterable<Entity<SRCID>>> L.withEagerly(dtoClass: KClass<*>): L {
    val props = BaseDatabase.eagerCollector?.invoke(dtoClass)
        ?: error("Eager collector is not registered. Use Database.registerEagerCollector() to register it.")

    return this.with(*props)
}

/**
 * Загружает связанные сущности для одного объекта, необходимые для создания DTO указанного типа.
 *
 * Пример использования:
 *
 * ```kotlin
 * User.findByIdOrThrow(userId)
 *     .loadEagerly(UserDTOs.Details::class)
 *     .toDTO<UserDTOs.Details>()
 * ```
 *
 * @param dtoClass Класс DTO, для которого нужно загрузить связанные сущности
 * @param SRCID Тип идентификатора исходной сущности
 * @param SRC Тип исходной сущности
 *
 * @return Исходная сущность с предварительно загруженными связанными объектами
 * */
context(Transaction)
fun <SRCID : Any, SRC : Entity<SRCID>> SRC.loadEagerly(dtoClass: KClass<*>): SRC {
    return this.apply {
        listOf(this).withEagerly(dtoClass)
    }
}