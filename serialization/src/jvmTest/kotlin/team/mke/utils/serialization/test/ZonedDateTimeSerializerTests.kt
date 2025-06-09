package team.mke.utils.serialization.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import ru.raysmith.utils.wrap
import team.mke.utils.serialization.ZonedDateTimeSerializer
import team.mke.utils.utcZoneId
import team.mke.utils.yekaZoneId
import java.time.ZonedDateTime

class ZonedDateTimeSerializerTests : FreeSpec({

    val pairs = listOf(
        "2000-05-01T12:30:05.000000123Z" to ZonedDateTime.of(2000, 5, 1, 12, 30, 5, 123, utcZoneId),
        "2000-05-01T12:30:00Z" to ZonedDateTime.of(2000, 5, 1, 12, 30, 0, 0, utcZoneId),
        "2000-05-01T12:30:00+05:00" to ZonedDateTime.of(2000, 5, 1, 12, 30, 0, 0, yekaZoneId),
    )

    "serialization" {
        pairs.forEach { (str, date) ->
            Json.encodeToString(ZonedDateTimeSerializer, date) shouldBe str.wrap('"')
        }
    }

    "deserialization" {
        pairs.forEach { (str, date) ->
            Json.decodeFromString(ZonedDateTimeSerializer, str.wrap('"')) shouldBe date
        }
    }
})