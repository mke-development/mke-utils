package team.mke.utils.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.datetime
import ru.raysmith.utils.nowZoned
import java.time.ZonedDateTime

/** Represents an entity that can't be removed from the database */
abstract class NotDeletableEntity<T : Any>(id: EntityID<T>, table: NotDeletableTable<T>) : Entity<T>(id) {
    var dateDeleted: ZonedDateTime? by table.dateDeleted

    override fun delete() {
        dateDeleted = nowZoned()
    }

    fun isDeleted() = dateDeleted != null
}

/** Represents table for [NotDeletableEntity] */
abstract class NotDeletableTable<T : Any>(name: String = "") : IdTable<T>(name) {

    val dateDeleted = datetime("date_deleted").nullable().index().transformToZonedDateTime()

    /** Returns path of a query that reflect the validity of the entity for any selection */
    open fun validQueryExpression(): Op<Boolean> = dateDeleted.isNull()
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