package team.mke.utils.serialization.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import team.mke.utils.json.json
import team.mke.utils.serialization.NullStringSerializer
import java.math.BigDecimal

class NullStringSerializerTests : FreeSpec({
    "NullStringSerializer with ignoreCase = true should deserialize \"NULL\" and \"Null\" as null" {
        val res1 = json.decodeFromString<CustomModel>("""{"num":"NULL"}""")
        res1.num.shouldBeNull()

        val res2 = json.decodeFromString<CustomModel>("""{"num":"Null"}""")
        res2.num.shouldBeNull()

        val res3 = json.decodeFromString<CustomModel>("""{"num":"42"}""")
        res3.num shouldBe 42
    }
}) {
    companion object {
        object CustomCaseInsensitiveSerializer : NullStringSerializer<Int>(
            Int.serializer(),
            { it.toInt() },
            ignoreCase = true
        )

        @Serializable
        data class CustomModel(
            @Serializable(with = CustomCaseInsensitiveSerializer::class)
            val num: Int?
        )
    }
}
