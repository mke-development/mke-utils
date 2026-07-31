package team.mke.utils.crashinterceptor.impl

import org.slf4j.Logger
import team.mke.utils.crashinterceptor.CrashInterceptor

/**
 * Test purpose implementation of [CrashInterceptor] that counts the number of times it was called and
 * stores the last parameters it was called with.
 * */
object TestCrashInterceptor : CrashInterceptor<CrashInterceptorConfigImpl> {

    /**
     * Total number of times the [intercept] was called.
     * */
    @set:Synchronized
    var totalIntercepted = 0
        private set

    /**
     * Total number of times the [message] was called.
     * */
    @set:Synchronized
    var totalMessaged = 0
        private set

    /**
     * Total number of times either [intercept] or [message] was called.
     * */
    @set:Synchronized
    var totalEntries = 0
        private set

    /**
     * The last [Throwable] that was passed to the [intercept] method.
     * */
    @set:Synchronized
    var lastThrowable: Throwable? = null
        private set

    /**
     * The last [Logger] that was passed to either the [intercept] or [message] method.
     * */
    @set:Synchronized
    var lastLogger: Logger? = null
        private set

    /**
     * The last message that was passed to either the [intercept] or [message] method.
     * */
    @set:Synchronized
    var lastMessage: String? = null
        private set

    /**
     * The last tags that were passed to either the [intercept] or [message] method.
     * */
    @set:Synchronized
    var lastTags: Map<String, Any?>? = null
        private set

    /**
     * Clears all the stored data and resets the counters
     * */
    fun clear() {
        totalIntercepted = 0
        totalMessaged = 0
        totalEntries = 0
        lastThrowable = null
        lastLogger = null
        lastMessage = null
        lastTags = null
    }

    override fun intercept(e: Throwable, logger: Logger, message: String?, printStackTrace: Boolean, tags: Map<String, Any?>?) {
        lastThrowable = e
        lastLogger = logger
        lastMessage = message
        lastTags = tags
        totalIntercepted++
        totalEntries++

        val message = "[test] $message ${tags ?:  ""}".trim()
        if (printStackTrace) {
            logger.error(message, e)
        } else {
            logger.error(message)
        }
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
