package team.mke.utils

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.time.Duration

/**
 * Returns a [Duration] with a random percentage of time added or subtracted, not exceeding [factorial]
 * */
fun Duration.rand(factorial: Double = 0.1): Duration {
    if (this == Duration.ZERO) return this

    require(factorial in 0.0..<1.0) { "factorial should be more or equal than 0.0 and less than 1.0" }

    return times(Random.nextDouble(1.0 - factorial, 1.0 + factorial))
}

/**
 * Returns the sum of all [BigDecimal]s in the list.
 * */
fun List<BigDecimal>.sum(): BigDecimal = sumOf { it }

/**
 * Returns the average of all [BigDecimal]s in the list, rounded according to the specified [roundingMode].
 * */
fun List<BigDecimal>.avg(roundingMode: RoundingMode = RoundingMode.HALF_UP): BigDecimal =
    sumOf { it }.divide(BigDecimal(size), roundingMode)

/**
 * Recursively searches for the first cause of the exception that is an instance of the specified [classes].
 * Returns the found cause or `null` if no such cause is found.
 * */
fun Throwable.findCause(vararg classes: KClass<*>): Throwable? {
    if (cause == null) return null
    if (classes.any { cause!!::class.isSubclassOf(it) }) return cause!!
    return cause!!.findCause(*classes)
}

/**
 * Recursively searches for the first cause of the exception that is an instance of the specified type [T].
 * Returns the found cause or `null` if no such cause is found.
 * */
inline fun <reified T> Throwable.findCause(): Throwable? {
    return findCause(T::class)
}
