package team.mke.utils.ktor.ext.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import team.mke.utils.ktor.ext.findOrSet

class AttributesTests : FreeSpec({
    val testKey = AttributeKey<String>("testKey")

    "findOrSet should return existing value and not evaluate block" {
        val attributes = Attributes(concurrent = false)
        attributes.put(testKey, "initial")

        var evaluated = false
        val result = attributes.findOrSet(testKey) {
            evaluated = true
            "computed"
        }

        result shouldBe "initial"
        evaluated shouldBe false
        attributes.getOrNull(testKey) shouldBe "initial"
    }

    "findOrSet should evaluate block, store and return value when key is absent" {
        val attributes = Attributes(concurrent = false)

        var evaluated = false
        val result = attributes.findOrSet(testKey) {
            evaluated = true
            "computed"
        }

        result shouldBe "computed"
        evaluated shouldBe true
        attributes.getOrNull(testKey) shouldBe "computed"
    }

    "findOrSet should return null and not store anything when block returns null" {
        val attributes = Attributes(concurrent = false)

        var evaluated = false
        val result = attributes.findOrSet(testKey) {
            evaluated = true
            null
        }

        result shouldBe null
        evaluated shouldBe true
        attributes.contains(testKey) shouldBe false
        attributes.getOrNull(testKey) shouldBe null
    }
})
