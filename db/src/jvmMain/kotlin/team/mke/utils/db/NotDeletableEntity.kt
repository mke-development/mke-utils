package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.javatime.datetime
import ru.raysmith.utils.nowZoned
import java.time.ZonedDateTime

interface TableWithValidExpression {

    /** Returns path of a query that reflect the validity of the entity for any selection */
    fun validQueryExpression(): Op<Boolean>
}

/** Represents an entity that can't be removed from the database */
abstract class NotDeletableEntity<T : Any>(id: EntityID<T>, table: NotDeletableTable<T>) : Entity<T>(id) {
    var dateDeleted: ZonedDateTime? by table.dateDeleted

    override fun delete() {
        dateDeleted = nowZoned()
    }

    fun isDeleted() = dateDeleted != null
}

/** Represents table for [NotDeletableEntity] */
abstract class NotDeletableTable<T : Any>(name: String = "") : IdTable<T>(name), TableWithValidExpression {

    val dateDeleted = datetime("date_deleted"). nullable().index().transformToZonedDateTime()

    override fun validQueryExpression(): Op<Boolean> = dateDeleted.isNull()
}

/** Represents table for [NotDeletableEntity] with integer id */
abstract class NotDeletableIntIdTable(name: String = "", columnName: String = "id") : NotDeletableTable<Int>(name) {
    final override val id: Column<EntityID<Int>> = integer(columnName).autoIncrement().entityId()
    final override val primaryKey = PrimaryKey(id)
}

/**
 * Base class responsible for the management of [NotDeletableEntity] instances and the maintenance of their relation to
 * the provided table.
 * */
abstract class NotDeletableEntityClass<ID : Any, out T : NotDeletableEntity<ID>>(
    table: NotDeletableTable<ID>,
    entityType: Class<T>? = null,
    entityCtor: ((EntityID<ID>) -> T)? = null,
) : EntityClass<ID, T>(table as IdTable<ID>, entityType, entityCtor)