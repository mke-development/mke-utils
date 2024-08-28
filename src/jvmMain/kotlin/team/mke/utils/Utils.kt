@file:Suppress("unused")

package team.mke.utils

import org.slf4j.Logger
import team.mke.utils.crashinterceptor.CrashInterceptor
import team.mke.utils.logging.tags
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import kotlin.time.Duration

// TODO clear

val utcZoneId: ZoneId = ZoneId.of("Z")
val yekaZoneId: ZoneId = ZoneId.of("+05:00")

val shortDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))
val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.forLanguageTag("ru"))
val shortDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", Locale.forLanguageTag("ru"))
val hoursFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH", Locale.forLanguageTag("ru"))
val minutesFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("mm", Locale.forLanguageTag("ru"))

@OptIn(ExperimentalContracts::class)
inline fun <T> safe(crashInterceptor: CrashInterceptor<*>, logger: Logger, block: () -> T): T? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return try {
        block()
    } catch (e: CancellationException) {
        null
    } catch (e: Exception) {
        crashInterceptor.intercept(e, logger, null, *tags(e))
        null
    }
}

/**
 * Возвращает [Duration] к которому случайно добавлен или вычтен процент ([factorial]) времени
 * */
fun Duration.rand(factorial: Double = 0.1): Duration {
    if (this == Duration.ZERO) return this

    require(factorial in 0.0..<1.0) { "factorial should be more or equal than 0.0 and less than 1.0" }

    return times(Random.nextDouble(1.0 - factorial, 1.0 + factorial))
}

/** Sets system properties from jvm args */
fun argsToProperties(args: Array<String>) {
    args.forEach {
        if (it.contains("=")) {
            val (key, value) = it.split("=")
            System.setProperty(key, value)
        }
    }
}