package team.mke.utils.test.serialization

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import team.mke.utils.serialization.EnumFallbackSerializer
import team.mke.utils.test.crashInterceptor
import team.mke.utils.test.logger

class EnumFallbackSerializerTests : FreeSpec({

    beforeSpec {
        crashInterceptor.totalMessaged shouldBe 0
    }

    afterSpec {
        crashInterceptor.clear()
    }

    "EnumFallbackSerializer should serialize exist value" {

        val expected = Foo.BAR
        val actual = Json.decodeFromString<Foo>("\"BAR\"")

        actual shouldBe expected
        crashInterceptor.totalMessaged shouldBe 0
    }

    "EnumFallbackSerializer should return fallback on non exist value" {
        crashInterceptor.totalMessaged shouldBe 0

        val expected = Foo.UNKNOWN
        val actual = Json.decodeFromString<Foo>("\"ABC\"")

        actual shouldBe expected
        crashInterceptor.totalMessaged shouldBe 1
    }
}) {
    companion object {

        private object FooSerializer : EnumFallbackSerializer<Foo>(crashInterceptor, logger, Foo::class, Foo.UNKNOWN)

        @Serializable(with = FooSerializer::class)
        private enum class Foo {
            FOO, BAR, UNKNOWN
        }
    }
}