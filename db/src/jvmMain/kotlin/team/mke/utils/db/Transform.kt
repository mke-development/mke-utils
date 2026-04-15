package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import team.mke.utils.defaultTimeZone
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneId

context(_: Table)
fun Column<LocalDateTime>.transformToZonedDateTime(
    timeZoneId: ZoneId = defaultTimeZone
) = with(table) {
    transform(
        wrap = { it.atZone(timeZoneId) },
        unwrap = { it.withZoneSameInstant(timeZoneId).toLocalDateTime()  }
    )
}

context(_: Table)
fun Column<Double>.transformToBigDecimal(
    scale: Int = 8, roundingMode: RoundingMode = RoundingMode.HALF_UP
) = with(table) {
    transform(
        wrap = { it.toBigDecimal().setScale(scale, roundingMode) },
        unwrap = { it.toDouble() }
    )
}
