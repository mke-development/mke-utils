package team.mke.utils.crashinterceptor.impl

import org.slf4j.Logger
import org.slf4j.MDC
import team.mke.utils.crashinterceptor.CrashInterceptor
import java.util.UUID

/**
 * Implementation of [CrashInterceptor] for development.
 * Logs errors with tags to the console.
 *
 * This interceptor puts a unique log ID (`log_id`) in the MDC for each intercepted error and message
 * */
object DevCrashInterceptor : CrashInterceptor<CrashInterceptorConfigImpl> {
    override fun intercept(e: Throwable, logger: Logger, message: String?, printStackTrace: Boolean, tags: Map<String, Any?>?) {
        MDC.put("log_id", UUID.randomUUID().toString())

        val message = "${message ?: e.message} (${tags ?: "[]"})"
        if (printStackTrace) {
            logger.error(message, e)
        } else {
            logger.error(message)
        }
    }

    override fun message(message: String, logger: Logger, tags: Map<String, Any?>?) {
        MDC.put("log_id", UUID.randomUUID().toString())
        logger.warn(message)
    }
}
