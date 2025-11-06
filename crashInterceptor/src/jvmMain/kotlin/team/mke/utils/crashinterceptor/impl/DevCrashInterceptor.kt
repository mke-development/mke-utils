package team.mke.utils.crashinterceptor.impl

import org.slf4j.Logger
import team.mke.utils.crashinterceptor.CrashInterceptor

/** Реализация [team.mke.utils.crashinterceptor.CrashInterceptor] для разработки. Выводит ошибки с тегами в консоль */
object DevCrashInterceptor : CrashInterceptor<CrashInterceptorConfigImpl> {
    override fun intercept(e: Throwable, logger: Logger, message: String?, printStackTrace: Boolean, tags: Map<String, Any?>?) {
        val message = "${message ?: e.message} (${tags ?: "[]"})"
        if (printStackTrace) {
            logger.error(message, e)
        } else {
            logger.error(message)
        }
    }

    override fun message(message: String, logger: Logger, tags: Map<String, Any?>?) {
        logger.warn(message)
    }
}