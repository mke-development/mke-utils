package team.mke.utils.db

import org.jetbrains.exposed.dao.ColumnWithTransform
import org.jetbrains.exposed.sql.Column
import team.mke.utils.env.env
import java.time.LocalDateTime
import java.time.ZoneId

private val defaultTimeZone by env("TIME_ZONE", ZoneId.systemDefault()) { ZoneId.of(it) }

@JvmName("transformToZonedDateTimeNotNull")
fun Column<LocalDateTime>.transformToZonedDateTime(timeZoneId: ZoneId = defaultTimeZone) =
    ColumnWithTransform(this, { it.withZoneSameInstant(timeZoneId).toLocalDateTime() }, { it.atZone(timeZoneId) })

fun Column<LocalDateTime?>.transformToZonedDateTime(timeZoneId: ZoneId = defaultTimeZone) =
    ColumnWithTransform(this, { it?.withZoneSameInstant(timeZoneId)?.toLocalDateTime() }, { it?.atZone(timeZoneId) })