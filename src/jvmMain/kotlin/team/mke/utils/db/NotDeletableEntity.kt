package team.mke.utils.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.javatime.datetime
import ru.raysmith.utils.nowZoned
import java.time.ZonedDateTime

/** Represents an entity that can't be removed from the database */
abstract class NotDeletableEntity<T : Comparable<T>>(id: EntityID<T>, table: NotDeletableTable<T>) : Entity<T>(id) {
    var dateDeleted: ZonedDateTime? by table.dateDeleted

    override fun delete() {
        dateDeleted = nowZoned()
    }

    fun isDeleted() = dateDeleted != null
}

/** Represents table for [NotDeletableEntity] */
abstract class NotDeletableTable<T : Comparable<T>>(name: String = "") : IdTable<T>(name) {

    val dateDeleted = datetime("date_deleted").nullable().index().transformToZonedDateTime()

    /** Returns path of a query that reflect the validity of the entity for any selection */
    open fun validQueryExpression(): Op<Boolean> = dateDeleted.isNotNull()
}

abstract class NotDeletableIntIdTable(name: String = "", columnName: String = "id") : NotDeletableTable<Int>(name) {
    final override val id: Column<EntityID<Int>> = integer(columnName).autoIncrement().entityId()
    final override val primaryKey = PrimaryKey(id)
}