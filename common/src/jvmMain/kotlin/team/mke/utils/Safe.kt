package team.mke.utils

import org.slf4j.Logger
import team.mke.utils.crashinterceptor.CrashInterceptor
import team.mke.utils.logging.ErrorTag
import team.mke.utils.logging.tags
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

/**
 * Executes the given [block] and intercepts any exceptions that occur during its execution using the provided
 * [crashInterceptor]. If a [CancellationException] is thrown, it is caught and `null` is returned without logging.
 *
 * @param crashInterceptor The [CrashInterceptor] to use for intercepting exceptions.
 * @param logger The [Logger] to use for logging exceptions.
 * @param tags A lambda that returns an array of [ErrorTag]s to be included in the [logger] and the [crashInterceptor]
 *  when an exception is caught. The lambda receives the caught exception as a parameter.
 * @param printStacktrace A boolean flag indicating whether to print the stack trace of the caught exception.
 *  Defaults to `true`.
 * @param block The block of code to execute safely.
 * */
@OptIn(ExperimentalContracts::class)
inline fun <T> safe(
    crashInterceptor: CrashInterceptor<*>,
    logger: Logger,
    tags: (e: Exception) -> Array<ErrorTag> = { arrayOf() },
    printStacktrace: Boolean = true,
    block: () -> T
): T? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return try {
        block()
    } catch (_: CancellationException) {
        null
    } catch (e: Exception) {
        crashInterceptor.intercept(e, logger, null, printStacktrace, *tags(e, *tags(e)))
        null
    }
}
