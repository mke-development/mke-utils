package team.mke.utils.serialization.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import team.mke.utils.serialization.BigDecimalSerializer
import java.math.BigDecimal
import java.math.RoundingMode

class BigDecimalSerializerTests : FreeSpec({
    "serialization" {
        Json.encodeToString(BigDecimalSerializer, BigDecimal.valueOf(0)) shouldBe "0.0"
        Json.encodeToString(BigDecimalSerializer, BigDecimal.valueOf(0.1)) shouldBe "0.1"
        Json.encodeToString(BigDecimalSerializer, BigDecimal.valueOf(0.1).setScale(10, RoundingMode.HALF_UP))
            .shouldBe("0.1000000000")
        Json.encodeToString(BigDecimalSerializer, BigDecimal.valueOf(0.123456789)) shouldBe "0.123456789"
    }

    "deserialization" {
        Json.decodeFromString(BigDecimalSerializer, "0") shouldBe BigDecimal.ZERO
        Json.decodeFromString(BigDecimalSerializer, "0.1") shouldBe BigDecimal("0.1")
        Json.decodeFromString(BigDecimalSerializer, "0.123456789") shouldBe BigDecimal("0.123456789")
        Json.decodeFromString(BigDecimalSerializer, "0.000001") shouldBe BigDecimal("0.000001")
    }
})
