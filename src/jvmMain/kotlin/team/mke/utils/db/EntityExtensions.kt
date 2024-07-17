package team.mke.utils.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Alias
import org.jetbrains.exposed.sql.Column
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

/** Return this entity or throw [EntityNotFoundException] if entity is null */
@Suppress("UNCHECKED_CAST")
inline fun <ID : Comparable<ID>, reified T : Entity<ID>> T?.orThrow(id: ID) = this ?: run {
    val entityClass = T::class.companionObject?.objectInstance as EntityClass<ID, *>
    throw EntityNotFoundException(EntityID(id, entityClass.table), entityClass)
}

@Suppress("UNCHECKED_CAST")
inline fun <ID: Comparable<ID>, REF: Comparable<REF>, reified R : Entity<REF>, S : Entity<ID>> S.wrapRowOrDefault(
    alias: Alias<IdTable<ID>>? = null, defaultValue: S.() -> R?
): R? {
    val entityClass = R::class.companionObjectInstance as EntityClass<REF, R>

    val cols = (alias ?: entityClass.table).columns
    val b1 = readValues.hasValues(cols)

    val ex = alias?.let { it[entityClass.table.id] } ?: entityClass.table.id
    val b2 = readValues.getOrNull(ex) != null

    return if (b1 && b2) {
        if (alias != null) {
            entityClass.wrapRow(readValues, alias)
        } else {
            entityClass.wrapRow(readValues)
        }
    }
    else this.defaultValue()
}

context(E)
inline fun <ID : Comparable<ID>, reified E : Entity<ID>, RID : Comparable<RID>, reified R : Entity<RID>> EntityClass<RID, R>.optionalReferencedOn(
    column: Column<EntityID<ID>?>,
    alias: Alias<IdTable<ID>>
): ReadWriteProperty<Any?, R?> {
    val entity = this@E

    return object : ReadWriteProperty<Any?, R?> {
        val ref = optionalReferencedOn(column)

        override fun getValue(thisRef: Any?, property: KProperty<*>): R? {
            return wrapRowOrDefault(alias) {
                ref.getValue(this, property)
            }
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: R?) {
            ref.setValue(entity, property, value)
        }
    }
}