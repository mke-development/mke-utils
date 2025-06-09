package team.mke.utils.env.test

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.system.withSystemProperty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import team.mke.utils.env.Environment
import team.mke.utils.env.env
import team.mke.utils.env.envRequired
import team.mke.utils.env.isTestEnvironment

class EnvironmentTests : FreeSpec({

    fun withSomeEnv(block: () -> Unit) {
        withSystemProperty("SOME_ENV", "123") {
            block()
        }
    }

    "Environment" {
        Environment.value shouldBe Environment.DEV
        Environment.isDev() shouldBe true
        env shouldBe Environment.DEV
    }

    "isTestEnvironment" {
        isTestEnvironment() shouldBe false
        withSystemProperty("IS_TEST_ENVIRONMENT", "true") {
            isTestEnvironment() shouldBe true
        }
    }

    "optional environment delegate should return value" {
        withSomeEnv {
            val value by env("SOME_ENV")
            value shouldBe "123"
        }
    }

    "optional environment delegate should return null if environment not set" {
        val value by env("SOME_ENV")
        value shouldBe null
    }

    "required environment delegate should return value" {
        withSomeEnv {
            val value by envRequired("SOME_ENV")
            value shouldBe "123"
        }
    }

    "required environment delegate should throw if environment not set" {
        shouldThrow<IllegalStateException> {
            val value by envRequired("SOME_ENV")
        }
    }

    "environment delegate should use default value if environment not set" {
        val value by env("SOME_ENV", "321")
        value shouldBe "321"
    }

    "environment delegate should transform value" {
        withSomeEnv {
            val value by env("SOME_ENV") { it.toInt() }
            value shouldBe 123
        }
    }
})