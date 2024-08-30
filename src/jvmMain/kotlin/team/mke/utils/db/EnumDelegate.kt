package team.mke.utils.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class EnumTable<E : Enum<E>>(
    name: String, enumClass: KClass<E>,
    enumName: String = enumClass.simpleName?.let { it.replaceFirstChar { c -> c.lowercase() } } ?: error("enum should not be anonymous")
) : IntIdTable(name) {
    init {
        check(enumClass.java.isEnum) { "enumClass should be Enum class" }
    }

    val value = enumerationByName(enumName, 255, enumClass)
}

abstract class EnumEntity<E : Enum<E>>(id: EntityID<Int>, table: EnumTable<E>) : IntEntity(id) {
    var value by table.value
}

abstract class EnumRelationshipTable<E : Enum<E>>(name: String, columnName: String, table: EnumTable<E>): Table(name) {
    val value = reference(columnName, table)
}

/**
 * Создает делегат для списков enum.
 *
 * @param entity DAO
 * @param relationshipTable Таблица отношений
 * */
inline fun <ID: Comparable<ID>, T : Entity<ID>, ENTITY : EnumEntity<E>, reified E : Enum<E>, R : EnumRelationshipTable<E>> T.enumDelegate(
    entity: IntEntityClass<ENTITY>, relationshipTable: R, alias: ExpressionAlias<String>? = null,
    noinline find: (table: R) -> Op<Boolean>,
): EnumDelegate<ID, T, ENTITY, E, R> {
    check(entity.table is EnumTable<*>) { "Table ${entity.table.tableName} of entity ${entity::class.simpleName} should be EnumTable" }

    @Suppress("UNCHECKED_CAST")
    return EnumDelegate(E::class, this, entity.table as EnumTable<E>, entity, relationshipTable, find, alias)
}

/**
 * Делегат для списков enum.
 *
 * @see [enumDelegate]
 * */
class EnumDelegate<CID : Comparable<CID>, C : Entity<CID>, ENTITY : EnumEntity<E>, E : Enum<E>, R : EnumRelationshipTable<E>>(
    val kClass: KClass<E>,
    val context: C,
    val table: EnumTable<E>, val enumEntityCompanion: IntEntityClass<ENTITY>, val relationshipTable: R,
    val find: (table: R) -> Op<Boolean>, val alias: ExpressionAlias<String>? = null,
) {
    val enumConstants by lazy { kClass.java.enumConstants }

    val viaDelegate by lazy {
        with(context) {
            enumEntityCompanion.via(relationshipTable)
        }
    }

    operator fun getValue(thisRef: C, property: KProperty<*>): List<E> {
        if (alias != null && context.readValues.hasValue(alias)) {
            return context.readValues.getOrNull(alias)?.split(",")?.filterNot { it.isEmpty() }?.map { v ->
                enumConstants.first { it.name == v }
            } ?: emptyList()
        }

        return viaDelegate.getValue(context, property).map { it.value }
    }
    operator fun setValue(thisRef: C, property: KProperty<*>, value: List<E>?) {
        transaction {
            val currentValue = getValue(thisRef, property)
            if (value != null && currentValue.containsAll(value) || value.isNullOrEmpty() && currentValue.isEmpty()) {
                return@transaction
            }

            relationshipTable.deleteWhere { find(relationshipTable) }

            if (value != null) {
                val foundQuery = enumEntityCompanion.find { table.value.inList(value) }.toList()
                if (foundQuery.size == value.size) {
                    viaDelegate.setValue(context, property, SizedCollection(foundQuery))
                } else {
                    val found = foundQuery.map { it.value }
                    val missed = value.filter { it !in found }.map { missed ->
                        enumEntityCompanion.new {
                            this.value = missed
                        }
                    }
                    viaDelegate.setValue(context, property, SizedCollection(foundQuery + missed))
                }
            }
        }
    }
}