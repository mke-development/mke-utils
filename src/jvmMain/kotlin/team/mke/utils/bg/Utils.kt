package team.mke.utils.bg

import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.Logger
import team.mke.utils.env.env
import ru.raysmith.utils.ms
import ru.raysmith.utils.uuid
import ru.raysmith.utils.now
import ru.raysmith.utils.today
import team.mke.utils.crashinterceptor.CrashInterceptor
import team.mke.utils.safe
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

/**
 * Создает и регистрирует фоновый процесс из [block]
 *
 * @param name Имя процесса
 * @param startDelay задержка перед запуском
 * @param logger логгер для вывода debug информации
 * @param start если false, то процесс не будет запущен
 * @param onCancel вызывается при отмене процесса
 * @param id уникальный идентификатор
 * */
fun bg(
    crashInterceptor: CrashInterceptor<*>,
    name: String,
    startDelay: Duration = 0.seconds,
    logger: Logger = Background.logger,
    start: Boolean = true,
    onCancel: () -> Unit = {},
    id: String = uuid(),
    block: suspend BackgroundProcess.() -> Unit
) = bgWhile(crashInterceptor, name, { 0.seconds }, startDelay, logger, start, onCancel, id, 1, block)

/**
 * Создает и регистрирует цикличный фоновый процесс из [block]
 *
 * @param name Имя процесса
 * @param delay задержка между итерациями
 * @param startDelay задержка перед запуском
 * @param logger логгер для вывода debug информации
 * @param start если false, то процесс не будет запущен
 * @param onCancel вызывается при отмене процесса
 * @param id уникальный идентификатор
 * @param maxIteration максимальное количество итераций. -1 для отсутствия лимита
 * */
fun bgWhile(
    crashInterceptor: CrashInterceptor<*>,
    name: String,
    delay: () -> Duration,
    startDelay: Duration = 0.seconds,
    logger: Logger = Background.logger,
    start: Boolean = true,
    onCancel: () -> Unit = {},
    id: String = uuid(),
    maxIteration: Int = -1,
    block: suspend BackgroundProcess.() -> Unit
) = object : BaseBackgroundProcess(name, crashInterceptor, id = id, autoStarted = start) {
    override val mutex: Any = object {}
    override fun onCancel() {
        super.onCancel()
        onCancel()
    }
    override fun run() {
        synchronized(mutex) {
            if (startDelay == ZERO) {
                isReadyToRestart = false
            }
        }

        job = Background.scope.launch(handler + coroutineName) {
            delay(startDelay)
            while (isActive) {
                synchronized(mutex) { isReadyToRestart = false }
                safe(crashInterceptor, logger) {
                    block()
                }
                synchronized(mutex) { isReadyToRestart = true }
                iterations++
                if (maxIteration != -1 && iterations >= maxIteration) {
                    break
                }
                delay(delay())
                synchronized(mutex) { isReadyToRestart = false }
            }
        }
    }

    suspend fun delay(delay: Duration) {
        logger.debug("Delay for $name: {}", delay)
        kotlinx.coroutines.delay(delay)
    }
}.apply {
    if (start) start()
}

// TODO readme
//| name                           | type | required | description                                                                                                                                                                  |
//|--------------------------------|------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
//| BG_DAILY_EVENT_DELAY_TOLERANCE | int  |          | Время в секундах в течение которого _ежедневное_ событие будет считаться не пропущенным, когда запуск фонового процесса произошел после отметки времени. По умолчанию – 300. |
private val timeTolerance by env("BG_DAILY_EVENT_DELAY_TOLERANCE", 300) { it.toInt() }

/**
 * Возвращает время до ближайшего *ежедневного* события.
 *
 * @param lastInvocation дата последнего события
 * @param time время события
 * */
fun getDailyDelay(lastInvocation: LocalDate, time: LocalTime, now: LocalDateTime = now()): Duration {
    return when {
        lastInvocation == today() -> ChronoUnit.MILLIS.between(now, now.plusDays(1).with(time)).ms
        else -> {
            if (now.toLocalTime() <= time || ChronoUnit.MINUTES.between(time, now.toLocalTime()) <= timeTolerance.seconds.inWholeMinutes) {
                getDelayTo(now.toLocalDate(), time, now)
            } else {
                getDelayTo(now.plusDays(1).toLocalDate(), time, now)
            }
        }
    }
}

fun getDelayTo(date: LocalDate, time: LocalTime, now: LocalDateTime = now()) = ChronoUnit.MILLIS.between(now, date.atTime(LocalTime.MIN).with(time)).ms