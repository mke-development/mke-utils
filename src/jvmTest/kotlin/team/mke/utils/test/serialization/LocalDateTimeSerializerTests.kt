package team.mke.utils.test.serialization

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import ru.raysmith.utils.wrap
import team.mke.utils.serialization.LocalDateTimeSerializer
import java.time.LocalDateTime

class LocalDateTimeSerializerTests : FreeSpec({
    "serialization" {
        Json.encodeToString(LocalDateTimeSerializer, LocalDateTime.of(2000, 5, 1, 12, 30)) shouldBe
                "2000-05-01T12:30:00".wrap('"')
    }

    "deserialization" {
        Json.decodeFromString(LocalDateTimeSerializer, "2000-05-01T12:30:00".wrap('"')) shouldBe
                LocalDateTime.of(2000, 5, 1, 12, 30)
    }
})