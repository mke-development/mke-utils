package team.mke.utils.crashinterceptor

import org.slf4j.Logger
import team.mke.utils.crashinterceptor.CrashInterceptor
import team.mke.utils.crashinterceptor.CrashInterceptorConfigImpl

/** Реализация [CrashInterceptor] для разработки. Выводит ошибки с тегами в консоль */
object DevCrashInterceptor : CrashInterceptor<CrashInterceptorConfigImpl> {
    override fun intercept(e: Throwable, logger: Logger, message: String?, tags: Map<String, Any?>?) {
        logger.error("${message ?: e.message} (${tags ?: "[]"})", e)
    }

    override fun message(message: String, logger: Logger, tags: Map<String, Any?>?) {
        logger.warn(message)
    }
}