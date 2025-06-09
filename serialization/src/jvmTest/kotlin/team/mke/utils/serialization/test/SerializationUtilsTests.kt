package team.mke.utils.serialization.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import team.mke.utils.serialization.getEnumFieldAnnotation
import team.mke.utils.serialization.getSerialName

class SerializationUtilsTests : FreeSpec({
    "getSerialName" {
        Foo.BAR.getSerialName() shouldBe "bar"
    }
    "getEnumFieldAnnotation" {
        Foo.BAR.getEnumFieldAnnotation<SerialName>().apply {
            shouldNotBeNull()
            value shouldBe "bar"
        }
    }
}) {
    companion object {
        private enum class Foo {
            @SerialName("foo") FOO,
            @SerialName("bar") BAR
        }
    }
}