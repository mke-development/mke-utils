package team.mke.utils.serialization.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecodingException
import org.junit.jupiter.api.assertThrows
import team.mke.utils.serialization.BooleanAsIntSerializer


@OptIn(ExperimentalSerializationApi::class)
class BooleanAsIntSerializerTests : FreeSpec({
    "serialization" {
        Json.encodeToString(BooleanAsIntSerializer, false) shouldBe "0"
        Json.encodeToString(BooleanAsIntSerializer, true) shouldBe "1"

    }

    "deserialization" {
        Json.decodeFromString(BooleanAsIntSerializer, "0") shouldBe false
        Json.decodeFromString(BooleanAsIntSerializer, "1") shouldBe true
        assertThrows<JsonDecodingException> {
            Json.decodeFromString(BooleanAsIntSerializer, "2")
        }
    }
})
