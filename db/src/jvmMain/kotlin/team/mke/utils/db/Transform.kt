package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import team.mke.utils.defaultTimeZone
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneId

context(Table)
@JvmName("transformToZonedDateTimeNotNull")
fun Column<LocalDateTime>.transformToZonedDateTime(timeZoneId: ZoneId = defaultTimeZone) = transform(
    wrap = { it.atZone(timeZoneId) },
    unwrap = { it.withZoneSameInstant(timeZoneId).toLocalDateTime()  }
)

context(Table)
fun Column<LocalDateTime?>.transformToZonedDateTime(timeZoneId: ZoneId = defaultTimeZone) = transform(
    wrap = { it?.atZone(timeZoneId) },
    unwrap = { it?.withZoneSameInstant(timeZoneId)?.toLocalDateTime()  }
)

context(Table)
@JvmName("transformToBigDecimalNotNull")
fun Column<Double>.transformToBigDecimal(
    scale: Int = 8, roundingMode: RoundingMode = RoundingMode.HALF_UP
) = transform(
    wrap = { it.toBigDecimal().setScale(scale, roundingMode) },
    unwrap = { it.toDouble() }
)

context(Table)
fun Column<Double?>.transformToBigDecimal(
    scale: Int = 8, roundingMode: RoundingMode = RoundingMode.HALF_UP
) = transform(
    wrap = { it?.toBigDecimal()?.setScale(scale, roundingMode) },
    unwrap = { it?.toDouble() }
)
