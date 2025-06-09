package team.mke.utils.serialization.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import ru.raysmith.utils.wrap
import team.mke.utils.serialization.LocalTimeSerializer
import java.time.LocalTime

class LocalTimeSerializerTests : FreeSpec({
    "serialization" {
        Json.encodeToString(LocalTimeSerializer, LocalTime.of(12, 30)) shouldBe "12:30:00".wrap('"')
        Json.encodeToString(LocalTimeSerializer, LocalTime.of(12, 30, 10)) shouldBe "12:30:10".wrap('"')
    }

    "deserialization" {
        Json.decodeFromString(LocalTimeSerializer, "12:30:00".wrap('"')) shouldBe LocalTime.of(12, 30)
    }
})