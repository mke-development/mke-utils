package team.mke.utils.db

import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import team.mke.utils.db.eager.Prop
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Интерфейс для сбора полей, которые нужно загрузить при eager загрузке сущности.
 * */
interface EagerFieldsCollector {

    /**
     * Собирает поля для загрузки при eager загрузке сущности.
     * */
    fun collect(dtoClass: KClass<*>, entityClass: EntityClass<*, *>? = null): Array<Prop>
}

/**
 * Собирает поля для загрузки при eager загрузке сущности, используя reified тип.
 * */
inline fun <reified T> EagerFieldsCollector.collect(entityClass: EntityClass<*, *>? = null) = collect(T::class, entityClass)

/**
 * Собирает поля для загрузки при eager загрузке сущности, используя reified тип и добавляя их к существующему массиву
 * полей.
 * */
context(collector: EagerFieldsCollector)
inline fun <reified T> Array<KProperty1<out Entity<*>, Any?>>.collect(entityClass: EntityClass<*, *>? = null) =
    this + collector.collect(T::class, entityClass)
