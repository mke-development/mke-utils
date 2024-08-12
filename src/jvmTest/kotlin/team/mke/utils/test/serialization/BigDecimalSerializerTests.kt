package team.mke.utils.test.serialization

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import team.mke.utils.serialization.BigDecimalSerializer
import java.math.BigDecimal

class BigDecimalSerializerTests : FreeSpec({
    "serialization" {
        Json.encodeToString(BigDecimalSerializer, BigDecimal.valueOf(0)) shouldBe "0.0"
        Json.encodeToString(BigDecimalSerializer, BigDecimal.valueOf(0.1)) shouldBe "0.1"
        Json.encodeToString(BigDecimalSerializer, BigDecimal.valueOf(0.12345678)) shouldBe "0.12345678"
        Json.encodeToString(BigDecimalSerializer, BigDecimal.valueOf(0.123456781)) shouldBe "0.12345678"
        Json.encodeToString(BigDecimalSerializer, BigDecimal.valueOf(0.123456789)) shouldBe "0.12345679"
        Json.encodeToString(BigDecimalSerializer, BigDecimal.valueOf(0.1 + 0.2)) shouldBe "0.3"
        Json.encodeToString(BigDecimalSerializer, BigDecimal.valueOf(0.000001)) shouldBe "0.000001"
    }

    "deserialization" {
        Json.decodeFromString(BigDecimalSerializer, "0") shouldBe BigDecimal.ZERO
        Json.decodeFromString(BigDecimalSerializer, "0.1") shouldBe BigDecimal("0.1")
        Json.decodeFromString(BigDecimalSerializer, "0.12345678") shouldBe BigDecimal("0.12345678")
        Json.decodeFromString(BigDecimalSerializer, "0.123456781") shouldBe BigDecimal("0.123456781")
        Json.decodeFromString(BigDecimalSerializer, "0.123456789") shouldBe BigDecimal("0.123456789")
        Json.decodeFromString(BigDecimalSerializer, "0.000001") shouldBe BigDecimal("0.000001")
    }
})