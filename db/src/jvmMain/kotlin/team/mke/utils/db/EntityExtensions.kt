package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Alias
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation

/** Return this entity or throw [EntityNotFoundException] with [message] if entity is null */
@Suppress("UNCHECKED_CAST")
inline fun <ID : Any, reified T : Entity<ID>> T?.orThrow(id: ID? = null, message: String? = null) = this ?: run {
    val entityClass = T::class.companionObject?.objectInstance as EntityClass<ID, *>
    throw EntityNotFoundException(entityClass, id?.let { EntityID(it, entityClass.table) }, message)
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

inline fun <ID : Any, REFID: Any, reified REF : Entity<REFID>?, SOURCE : Entity<ID>, T : IdTable<ID>> SOURCE.wrapRowFromAliasOrDefault(
    alias: Alias<T>? = null, defaultValue: SOURCE.() -> REF
): REF {
    return if (alias != null) wrapRowOrDefault(alias) { defaultValue() }
    else defaultValue()
}

inline fun <ID : Any, SOURCE : Entity<ID>, T> SOURCE.wrapValueFromAliasOrDefault(
    alias: Expression<T>? = null, defaultValue: SOURCE.() -> T
): T {
    return if (alias != null) readValues.getOrNull(alias) ?: defaultValue()
    else defaultValue()
}

fun EntityClass<*, *>.entityName(): String? {
    val kClass = (javaClass.enclosingClass as Class<*>).kotlin
    return kClass.findAnnotation<EntityName>()?.name ?: kClass.simpleName
}

/** не найдена или больше не доступна */
const val entityNotFoundPostfixF = "не найдена или больше не доступна"

/** не найден или больше не доступен */
const val entityNotFoundPostfixM = "не найден или больше не доступен"

/** не найдено или больше не доступно */
const val entityNotFoundPostfixN = "не найдено или больше не доступно"

/** не найдены или больше не доступны */
const val entityNotFoundPostfixMultiple = "не найдены или больше не доступны"