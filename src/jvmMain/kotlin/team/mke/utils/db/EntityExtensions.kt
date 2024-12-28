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
inline fun <ID : Any, reified T : Entity<ID>> T?.orThrow(id: ID) = this ?: run {
    val entityClass = T::class.companionObject?.objectInstance as EntityClass<ID, *>
    throw EntityNotFoundException(EntityID(id, entityClass.table), entityClass)
}

/** Return this entity or throw [EntityNotFoundExceptionByColumn] if entity is null */
inline fun <V : Any?, reified T : Entity<*>> T?.orThrowBy(value: V, column: Column<*>) = this ?: run {
    val entityClass = T::class.companionObject?.objectInstance as EntityClass<*, *>
    throw EntityNotFoundExceptionByColumn(value, column, entityClass)
}

inline fun <ID : Any, REFID: Any, reified REF : Entity<REFID>?, SOURCE : Entity<ID>> SOURCE.wrapRowOrDefault(
    alias: Alias<IdTable<ID>>? = null, defaultValue: SOURCE.() -> REF
): REF {
    val entityClass = REF::class.companionObjectInstance as EntityClass<*, *>
    val columns = (alias ?: entityClass.table).columns

    val idColumn = alias?.let { it[entityClass.table.id] } ?: entityClass.table.id
    if (readValues.getOrNull(idColumn) == null) {
        return this.defaultValue()
    }

    if (!readValues.hasValues(columns)) {
        return this.defaultValue()
    }

    return if (alias != null) {
        entityClass.wrapRow(readValues, alias)
    } else {
        entityClass.wrapRow(readValues)
    } as REF
}

context(E)
inline fun <ID : Any, reified E : Entity<ID>, RID : Any, reified R : Entity<RID>> EntityClass<RID, R>.optionalReferencedOn(
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