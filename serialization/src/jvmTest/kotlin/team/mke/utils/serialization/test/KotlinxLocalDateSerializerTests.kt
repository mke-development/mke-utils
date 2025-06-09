package team.mke.utils.serialization.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import ru.raysmith.utils.wrap
import team.mke.utils.serialization.KotlinxLocalDateSerializer

class KotlinxLocalDateSerializerTests : FreeSpec({
    "serialization" {
        Json.encodeToString(KotlinxLocalDateSerializer, LocalDate(2000, 5, 1)) shouldBe "2000-05-01".wrap('"')
    }

    "deserialization" {
        Json.decodeFromString(KotlinxLocalDateSerializer, "2000-05-01".wrap('"')) shouldBe LocalDate(2000, 5, 1)
    }
})