import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.delay
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import team.mke.utils.bg.Background
import team.mke.utils.bg.bg
import team.mke.utils.bg.bgWhile
import team.mke.utils.crashinterceptor.TestCrashInterceptor
import team.mke.utils.test.crashInterceptor
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

    "background handle exception" {
        bg(crashInterceptor, "bg3", id = "3") {
            error("Some error")
        }

        delay(100)
        crashInterceptor.shouldBeInstanceOf<TestCrashInterceptor>()
            .totalIntercepted.shouldBe(1)
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

        assertDoesNotThrow {
            bg(crashInterceptor, "bg5", id = "5") {
                delay(Long.MAX_VALUE)
            }
        }
    }

    "not allow register same background before stopped" {
        bg(crashInterceptor, "bg6", id = "6") {
            delay(Long.MAX_VALUE)
        }

        assertThrows<IllegalStateException> {
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
})