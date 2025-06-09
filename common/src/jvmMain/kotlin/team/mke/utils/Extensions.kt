package team.mke.utils

import java.math.BigDecimal
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.time.Duration

/**
 * Возвращает [Duration] к которому случайно добавлен или вычтен процент времени в случайном размере
 * не превышающим ([factorial])
 * */
fun Duration.rand(factorial: Double = 0.1): Duration {
    if (this == Duration.ZERO) return this

    require(factorial in 0.0..<1.0) { "factorial should be more or equal than 0.0 and less than 1.0" }

    return times(Random.nextDouble(1.0 - factorial, 1.0 + factorial))
}

fun List<BigDecimal>.sum(): BigDecimal = sumOf { it }

fun Throwable.findCause(vararg classes: KClass<*>): Throwable? {
    if (cause == null) return null
    if (classes.any { cause!!::class.isSubclassOf(it) }) return cause!!
    return cause!!.findCause(*classes)
}

inline fun <reified T> Throwable.findCause(): Throwable? {
    return findCause(T::class)
}