package team.mke.utils.db.eager

import org.jetbrains.exposed.v1.dao.Entity
import kotlin.reflect.KProperty1

typealias Prop = KProperty1<out Entity<*>, Any?>

/**
 * Создаёт массив свойств из переданных аргументов для использования в загрузке связанных сущностей.
 *
 * Пример использования:
 *
 * ```kotlin
 * // В функции collect(dtoClass: KClass<*>)
 * UserDTOs.Details::class -> props(UserDTOs.Details::createdBy, UserDTOs.Details::updatedBy)
 * ```
 *
 * @param props Свойства сущностей, которые нужно включить в массив
 * @return Массив свойств для загрузки
 */
fun props(vararg props: Prop): Array<Prop> {
    return Array(props.size) { props[it] }
}