package team.mke.utils.crashinterceptor.impl

import org.slf4j.Logger
import team.mke.utils.crashinterceptor.CrashInterceptor

object TestCrashInterceptor : CrashInterceptor<CrashInterceptorConfigImpl> {

    @set:Synchronized
    var totalIntercepted = 0
        private set

    @set:Synchronized
    var totalMessaged = 0
        private set

    @set:Synchronized
    var totalEntries = 0
        private set

    @set:Synchronized
    var lastThrowable: Throwable? = null
        private set

    @set:Synchronized
    var lastLogger: Logger? = null
        private set

    @set:Synchronized
    var lastMessage: String? = null
        private set

    @set:Synchronized
    var lastTags: Map<String, Any?>? = null
        private set

    fun clear() {
        totalIntercepted = 0
        totalMessaged = 0
        totalEntries = 0
        lastThrowable = null
        lastLogger = null
        lastMessage = null
        lastTags = null
    }

    override fun intercept(e: Throwable, logger: Logger, message: String?, tags: Map<String, Any?>?) {
        lastThrowable = e
        lastLogger = logger
        lastMessage = message
        lastTags = tags
        totalIntercepted++
        totalEntries++
        logger.error("[test] $message ${tags ?:  ""}".trim(), e)
    }

    override fun message(message: String, logger: Logger, tags: Map<String, Any?>?) {
        lastMessage = message
        lastLogger = logger
        lastTags = tags
        totalMessaged++
        totalEntries++
        logger.warn("[test] $message ${tags ?: ""}".trim())
    }
}