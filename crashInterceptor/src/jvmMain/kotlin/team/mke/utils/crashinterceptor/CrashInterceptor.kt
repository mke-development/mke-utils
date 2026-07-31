package team.mke.utils.crashinterceptor

import org.slf4j.Logger

/**
 * Interface for intercepting crashes and messages for monitoring purposes
 * */
interface CrashInterceptor<T : CrashInterceptorConfig> {

    /**
     * Initialize the crash interceptor with the provided logger and configuration block.
     * */
    fun init(logger: Logger, config: T.() -> Unit = {}) {

    }

    /**
     * Intercept a crash with the provided throwable, logger, optional message, and tags.
     * */
    fun intercept(
        e: Throwable,
        logger: Logger,
        message: String? = null,
        printStackTrace: Boolean = true,
        tags: Map<String, Any?>? = null
    )

    /**
     * Intercept a crash with the provided throwable, logger, optional message, and tags.
     * */
    fun intercept(
        e: Throwable,
        logger: Logger,
        message: String? = null,
        printStackTrace: Boolean = true,
        vararg tags: Pair<String, Any?>
    ) = intercept(e, logger, message, printStackTrace, tags.toMap())

    /**
     * Report a message with the provided logger and optional tags.
     * */
    fun message(message: String, logger: Logger, tags: Map<String, Any?>? = null)

    /**
     * Report a message with the provided logger and optional tags.
     * */
    fun message(message: String, logger: Logger, vararg tags: Pair<String, Any?>) =
        message(message, logger, tags.toMap())
}
