package team.mke.utils.serialization.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import ru.raysmith.utils.wrap
import team.mke.utils.serialization.LocalDateSerializer
import java.time.LocalDate

class LocalDateSerializerTests : FreeSpec({
    "serialization" {
        Json.encodeToString(LocalDateSerializer, LocalDate.of(2000, 5, 1)) shouldBe "2000-05-01".wrap('"')
    }

    "deserialization" {
        Json.decodeFromString(LocalDateSerializer, "2000-05-01".wrap('"')) shouldBe LocalDate.of(2000, 5, 1)
    }
})