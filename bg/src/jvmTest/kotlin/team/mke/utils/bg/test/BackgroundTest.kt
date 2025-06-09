package team.mke.utils.bg.test

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import team.mke.utils.bg.Background
import team.mke.utils.bg.bg
import team.mke.utils.bg.bgWhile
import kotlin.time.Duration.Companion.seconds

class BackgroundTest : FreeSpec({

    beforeSpec {
        crashInterceptor.totalMessaged shouldBe 0
    }

    afterSpec {
        crashInterceptor.clear()
    }

    "background is registered" {
        bg(crashInterceptor, "bg1", id = "1") {
            delay(Long.MAX_VALUE)
        }

        Background.hasProcess("1").shouldBe(true)
        Background.findProcess("1").shouldNotBeNull()
    }

    "background stopped" {
        bg(crashInterceptor, "bg2", id = "2") {
            delay(Long.MAX_VALUE)
        }

        Background.findProcess("2").shouldNotBeNull().stop()
        Background.hasProcess("2").shouldBe(false)
    }

    "background should handle exception" {
        bg(crashInterceptor, "bg3", id = "3") {
            error("Some error")
        }.join()

        delay(100)
        crashInterceptor.totalIntercepted.shouldBe(1)
    }

    "background stopped after cancel" {
        val bg = bg(crashInterceptor, "bg4", id = "4") {
            delay(Long.MAX_VALUE)
        }

        delay(500)
        bg.cancel()
        bg.stop()
    }

    "allow to register same background after stopped" {
        val bg = bg(crashInterceptor, "bg5", id = "5") {
            delay(Long.MAX_VALUE)
        }

        bg.stop()

        shouldNotThrowAny {
            bg(crashInterceptor, "bg5", id = "5") {
                delay(Long.MAX_VALUE)
            }
        }
    }

    "not allow register same background before stopped" {
        bg(crashInterceptor, "bg6", id = "6") {
            delay(Long.MAX_VALUE)
        }

        shouldThrow<IllegalStateException> {
            bg(crashInterceptor, "bg6", id = "6") {
                delay(Long.MAX_VALUE)
            }
        }
    }

    "background restart should be delayed when is active" {
        val bg = bgWhile(crashInterceptor, "bg7", { 0.seconds }, id = "7") {
            println("bg 7 is active...")
            delay(1000)
        }

        delay(500)
        bg.restart() shouldBe false
        bg.stop()
    }

    "background restart should be restarted when is not active" {
        val bg = bgWhile(crashInterceptor, "bg8", { 1.seconds }, id = "8") {
            println("bg 8 is active...")
        }

        delay(500)
        bg.restart() shouldBe true
        bg.stop()
    }


    "background should be active when running" {
        val bg = bg(crashInterceptor, "bg9", id = "9", start = false) {
            delay(500)
        }

        bg.isActive shouldBe false
        bg.start()
        bg.isActive shouldBe true
        delay(600)
        bg.isActive shouldBe false
    }

    "cancelAndJoin should not throw" {
        val bg = bg(crashInterceptor, "bg10", id = "10") {
            delay(500)
        }

        shouldNotThrowAny {
            bg.cancelAndJoin()
        }
    }
})