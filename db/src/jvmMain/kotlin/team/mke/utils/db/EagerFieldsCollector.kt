package team.mke.utils.db

import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import team.mke.utils.db.eager.Prop
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface EagerFieldsCollector {
    fun collect(dtoClass: KClass<*>, entityClass: EntityClass<*, *>? = null): Array<Prop>
}

inline fun <reified T> EagerFieldsCollector.collect(entityClass: EntityClass<*, *>? = null) = collect(T::class, entityClass)

context(collector: EagerFieldsCollector)
inline fun <reified T> Array<KProperty1<out Entity<*>, Any?>>.collect(entityClass: EntityClass<*, *>? = null) =
    this + collector.collect(T::class, entityClass)
