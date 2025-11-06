package team.mke.utils

import org.slf4j.Logger
import team.mke.utils.crashinterceptor.CrashInterceptor
import team.mke.utils.logging.ErrorTag
import team.mke.utils.logging.tags
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalContracts::class)
inline fun <T> safe(
    crashInterceptor: CrashInterceptor<*>,
    logger: Logger,
    tags: () -> Array<ErrorTag> = { arrayOf() },
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
        crashInterceptor.intercept(e, logger, null, printStacktrace, *tags(e, *tags()))
        null
    }
}