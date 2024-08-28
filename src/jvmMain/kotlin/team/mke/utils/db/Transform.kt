package team.mke.utils.db

import org.jetbrains.exposed.dao.ColumnWithTransform
import org.jetbrains.exposed.sql.Column
import team.mke.utils.env.env
import team.mke.utils.serialization.BigDecimalSerializer
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneId

private val defaultTimeZone by env("TIME_ZONE", ZoneId.systemDefault()) { ZoneId.of(it) }

@JvmName("transformToZonedDateTimeNotNull")
fun Column<LocalDateTime>.transformToZonedDateTime(timeZoneId: ZoneId = defaultTimeZone) =
    ColumnWithTransform(this, { it.withZoneSameInstant(timeZoneId).toLocalDateTime() }, { it.atZone(timeZoneId) })

fun Column<LocalDateTime?>.transformToZonedDateTime(timeZoneId: ZoneId = defaultTimeZone) =
    ColumnWithTransform(this, { it?.withZoneSameInstant(timeZoneId)?.toLocalDateTime() }, { it?.atZone(timeZoneId) })

@JvmName("memoizedTransformToZonedDateTimeNotNull")
fun Column<LocalDateTime>.memoizedTransformToZonedDateTime(timeZoneId: ZoneId = defaultTimeZone) =
    ColumnWithTransform(this, { it.withZoneSameInstant(timeZoneId).toLocalDateTime() }, { it.atZone(timeZoneId) }, true)

fun Column<LocalDateTime?>.memoizedTransformToZonedDateTime(timeZoneId: ZoneId = defaultTimeZone) =
    ColumnWithTransform(this, { it?.withZoneSameInstant(timeZoneId)?.toLocalDateTime() }, { it?.atZone(timeZoneId) }, true)



@JvmName("transformToBigDecimalNotNull")
fun Column<Double>.transformToBigDecimal(scale: Int = BigDecimalSerializer.scale, roundingMode: RoundingMode = BigDecimalSerializer.roundingMode) =
    ColumnWithTransform(this, { it.toDouble() }, { it.toBigDecimal().setScale(scale, roundingMode) })

fun Column<Double?>.transformToBigDecimal(scale: Int = BigDecimalSerializer.scale, roundingMode: RoundingMode = BigDecimalSerializer.roundingMode) =
    ColumnWithTransform(this, { it?.toDouble() }, { it?.toBigDecimal()?.setScale(scale, roundingMode) })

@JvmName("memoizedTransformToBigDecimalNotNull")
fun Column<Double>.memoizedTransformToBigDecimal(scale: Int = BigDecimalSerializer.scale, roundingMode: RoundingMode = BigDecimalSerializer.roundingMode) =
    ColumnWithTransform(this, { it.toDouble() }, { it.toBigDecimal().setScale(scale, roundingMode) }, true)

fun Column<Double?>.memoizedTransformToBigDecimal(scale: Int = BigDecimalSerializer.scale, roundingMode: RoundingMode = BigDecimalSerializer.roundingMode) =
    ColumnWithTransform(this, { it?.toDouble() }, { it?.toBigDecimal()?.setScale(scale, roundingMode) }, true)