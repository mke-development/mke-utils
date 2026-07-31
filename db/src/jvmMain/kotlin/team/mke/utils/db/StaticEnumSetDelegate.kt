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
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

context(e: E)
inline fun <ID : Any, E : Entity<ID>, reified ENUM : Enum<ENUM>> staticEnumSetDelegate(
    relationshipTable: Table,
    entityColumn: Column<EntityID<ID>>? = null,
    enumColumn: Column<ENUM>? = null
) = StaticEnumSetDelegate(
    e,
    relationshipTable,
    ENUM::class,
    entityColumn,
    enumColumn
)

@Suppress("UNCHECKED_CAST")
class StaticEnumSetDelegate<ID : Any, E : Entity<ID>, ENUM : Enum<ENUM>>(
    val entity: E,
    val relationshipTable: Table,
    val enumClass: KClass<ENUM>,
    val entityColumn: Column<EntityID<ID>>? = null,
    val enumColumn: Column<ENUM>? = null
) : ReadWriteProperty<E, Set<ENUM>> {

    private val _entityColumn: Column<EntityID<ID>> by lazy {
        entityColumn
            ?: relationshipTable.columns.find { it.referee?.table == (entity.klass as EntityClass<ID, E>).table }
                    as? Column<EntityID<ID>>
            ?: error("Cannot find entity column in relationship table ${relationshipTable.tableName}")
    }

    private val _enumColumn: Column<ENUM> by lazy {
        enumColumn
            ?: relationshipTable.columns.find { (it.columnType as? EnumerationColumnType<*>)?.klass == enumClass }
                    as? Column<ENUM>
            ?: error("Cannot find enum column in relationship table ${relationshipTable.tableName}")
    }

    private var cache: Set<ENUM>? = null

    override fun getValue(thisRef: E, property: KProperty<*>): Set<ENUM> {

        if (cache == null) {
            cache = relationshipTable.select(_enumColumn)
                .where { _entityColumn.eq(entity.id) }
                .map { it[_enumColumn] }
                .toSet()
        }

        return cache!!
    }

    override fun setValue(thisRef: E, property: KProperty<*>, value: Set<ENUM>) {
        TransactionManager.current() // ensure we're in a transaction

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
