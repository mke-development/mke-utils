import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import ru.raysmith.utils.plus
import ru.raysmith.utils.now
import ru.raysmith.utils.today
import ru.raysmith.utils.tomorrow
import ru.raysmith.utils.yesterday
import team.mke.utils.bg.getDailyDelay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private fun time(h: Int, m: Int) = LocalTime.of(h, m)
private fun time(h: Int) = LocalTime.of(h, 0)

class GetDailyDelayTest : FreeSpec({

    beforeSpec {
        System.setProperty("BG_DAILY_EVENT_DELAY_TOLERANCE", "300")
        mockkStatic(::now)
        mockkStatic(::today)
        mockkStatic(::yesterday)
        mockkStatic(::tomorrow)

        every { today() } returns LocalDate.of(2000, 1, 20)
        every { yesterday() } returns LocalDate.of(2000, 1, 19)
        every { tomorrow() } returns LocalDate.of(2000, 1, 21)
    }

    fun day(lastInvocation: LocalDate, time: LocalTime) = (now() + getDailyDelay(lastInvocation, time)).toLocalDate()

    "getDailyDelay with time 18:00" {
        every { now() } returns LocalDateTime.of(today(), time(18))

        day(today(), time(0)).shouldBe(tomorrow())
        day(today(), time(1)).shouldBe(tomorrow())
        day(today(), time(17)).shouldBe(tomorrow())

        day(yesterday(), time(0)).shouldBe(tomorrow())
        day(yesterday(), time(1)).shouldBe(tomorrow())
        day(yesterday(), time(17)).shouldBe(tomorrow())
        day(yesterday(), time(17, 56)).shouldBe(today())
        day(yesterday(), time(23)).shouldBe(today())
    }

    "getDailyDelay with time 03:00" {
        every { now() } returns LocalDateTime.of(today(), time(3))

        day(today(), time(0)).shouldBe(tomorrow())
        day(today(), time(1)).shouldBe(tomorrow())

        day(yesterday(), time(0)).shouldBe(tomorrow())
        day(yesterday(), time(1)).shouldBe(tomorrow())
        day(yesterday(), time(2, 56)).shouldBe(today())
        day(yesterday(), time(3)).shouldBe(today())
        day(yesterday(), time(23)).shouldBe(today())
    }
})

