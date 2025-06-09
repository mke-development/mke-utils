package team.mke.utils.crashinterceptor

import org.slf4j.Logger

/** Перехватчик ошибок для мониторинга */
interface CrashInterceptor<T : CrashInterceptorConfig> {
    fun init(logger: Logger, config: T.() -> Unit = {}) {

    }

    fun intercept(e: Throwable, logger: Logger, message: String? = null, tags: Map<String, Any?>? = null)
    fun intercept(e: Throwable, logger: Logger, message: String? = null, vararg tags: Pair<String, Any?>) =
        intercept(e, logger, message, tags.toMap())

    fun message(message: String, logger: Logger, tags: Map<String, Any?>? = null)
    fun message(message: String, logger: Logger, vararg tags: Pair<String, Any?>) =
        message(message, logger, tags.toMap())
}