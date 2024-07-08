package team.mke.utils.test.serialization

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.assertThrows
import team.mke.utils.serialization.BooleanAsIntSerializer


class BooleanAsIntSerializerTests : FreeSpec({
    "serialization" {
        Json.encodeToString(BooleanAsIntSerializer, false) shouldBe "0"
        Json.encodeToString(BooleanAsIntSerializer, true) shouldBe "1"

    }

    "deserialization" {
        Json.decodeFromString(BooleanAsIntSerializer, "0") shouldBe false
        Json.decodeFromString(BooleanAsIntSerializer, "1") shouldBe true
        assertThrows<IllegalStateException> {
            Json.decodeFromString(BooleanAsIntSerializer, "2")
        }
    }
})