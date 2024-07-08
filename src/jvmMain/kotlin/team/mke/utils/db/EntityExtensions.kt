package team.mke.utils.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import org.jetbrains.exposed.dao.id.EntityID
import kotlin.reflect.full.companionObject

/** Return this entity or throw [EntityNotFoundException] if entity is null */
@Suppress("UNCHECKED_CAST")
inline fun <ID : Comparable<ID>, reified T : Entity<ID>> T?.orThrow(id: ID) = this ?: run {
    val entityClass = T::class.companionObject?.objectInstance as EntityClass<ID, *>
    throw EntityNotFoundException(EntityID(id, entityClass.table), entityClass)
}