package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.EnumerationColumnType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import ru.raysmith.utils.Cacheable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.time.Duration

context(e: E)
fun <ID : Any, E : Entity<ID>, ENUM : Enum<ENUM>> staticEnumSetDelegate(
    relationshipTable: Table,
    entityClass: EntityClass<ID, E>,
    enumClass: KClass<ENUM>,
    entityColumn: Column<EntityID<ID>>? = null,
    enumColumn: Column<ENUM>? = null
) = object : StaticEnumSetDelegate<ID, E, ENUM>(
    e,
    relationshipTable,
    entityClass,
    enumClass,
    entityColumn,
    enumColumn
) {}

@Suppress("UNCHECKED_CAST")
abstract class StaticEnumSetDelegate<ID : Any, E : Entity<ID>, ENUM : Enum<ENUM>>(
    val entity: E,
    val relationshipTable: Table,
    val entityClass: EntityClass<ID, E>,
    val enumClass: KClass<ENUM>,
    val entityColumn: Column<EntityID<ID>>? = null,
    val enumColumn: Column<ENUM>? = null
) : ReadWriteProperty<E, Set<ENUM>> {

    private val _entityColumn: Column<EntityID<ID>> by lazy {
        (entityColumn
            ?: relationshipTable.columns.find { it.referee?.table == entityClass.table }
            ?: error("Cannot find entity column in relationship table ${relationshipTable.tableName}"))
        as Column<EntityID<ID>>
    }

    private val _enumColumn: Column<ENUM> by lazy {
        (enumColumn
            ?: relationshipTable.columns.find { (it.columnType as? EnumerationColumnType<*>)?.klass == enumClass }
            ?: error("Cannot find enum column in relationship table ${relationshipTable.tableName}"))
        as Column<ENUM>
    }


    private var cache by Cacheable(Duration.INFINITE) {
        relationshipTable.selectAll()
            .where { _entityColumn.eq(entity.id) }
            .map {
                it[_enumColumn]
            }
            .toSet()
    }

    override fun getValue(thisRef: E, property: KProperty<*>): Set<ENUM> = cache

    override fun setValue(thisRef: E, property: KProperty<*>, value: Set<ENUM>) {
        relationshipTable.deleteWhere {
            _entityColumn.eq(entity.id)
        }
        relationshipTable.batchInsert(value) {
            this[_entityColumn] = entity.id
            this[_enumColumn] = it
        }
        cache = value
    }
}
