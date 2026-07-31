package team.mke.utils.crashinterceptor.impl.sentry

import io.sentry.IScope
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import org.slf4j.Logger
import org.slf4j.MDC
import team.mke.utils.env.Environment
import team.mke.utils.env.env
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass
import team.mke.utils.crashinterceptor.CrashInterceptor
import team.mke.utils.crashinterceptor.CrashInterceptorConfig
import java.util.UUID

/**
 * Implementation of [CrashInterceptor] using Sentry.
 *
 * This interceptor initializes Sentry with the provided DSN and configuration options, and intercepts crashes by
 * sending them to Sentry.
 * It also allows you to specify a blacklist of exception classes that should not be sent to Sentry, and to add custom
 * tags to the Sentry events.
 *
 * This interceptor puts a unique log ID (`log_id`) in the MDC for each intercepted error and message
 *
 * Note: By default, [CancellationException], [SocketTimeoutException] and [SocketException] are included in the
 * blacklist, as they often indicate network issues rather than application errors. You can remove them from the
 * blacklist with the [clearBlackList][SentryCrashInterceptor.Config.clearBlackList] method if you want to track these
 * exceptions in Sentry.
 *
 * Note: Make sure to set the environment to `prod` for your application to enable interceptors
 *
 * Example usage:
 * ```
 * val crashInterceptor = SentryCrashInterceptor()
 * crashInterceptor.init(logger) {
 *     dsn = "your-sentry-dsn"
 *     blackList(CancellationException::class, SocketException::class, SocketTimeoutException::class)
 *     options {
 *         // Additional Sentry options configuration
 *     }
 * }
 * ```
 * */
object SentryCrashInterceptor : CrashInterceptor<SentryCrashInterceptor.Config> {

    class Config : CrashInterceptorConfig {

        /**
         * Sentry DSN (Data Source Name).
         * */
        var dsn: String? = null

        private val _blackList: MutableSet<KClass<out Exception>> = mutableSetOf(
            CancellationException::class, SocketTimeoutException::class, SocketException::class
        )
        val blackList: Set<KClass<out Exception>>
            get () = _blackList


        /**
         * Add exception classes to the blacklist. Exceptions of these classes will not be sent to Sentry.
         * */
        fun blackList(vararg classes: KClass<out Exception>) {
            this._blackList.addAll(classes)
        }

        /**
         * Clear the blacklist, allowing all exceptions to be sent to Sentry.
         * */
        fun clearBlackList() {
            _blackList.clear()
        }

        internal var options: SentryOptions.() -> Unit = {}

        /**
         * Additional Sentry options configuration.
         * This block will be executed after the default options are set, allowing you to override them if necessary.
         * */
        fun options(block: SentryOptions.() -> Unit) {
            options = block
        }
    }

    private val config = Config()

    override fun init(logger: Logger, config: Config.() -> Unit) {
        this.config.apply(config)

        Sentry.init { options ->
            options.dsn = this.config.dsn
            options.tracesSampleRate = 1.0
            options.isDebug = !Environment.isProd()
            options.environment = env.name.lowercase()
            options.isEnableAutoSessionTracking = false
            options.isEnableUncaughtExceptionHandler = true

            options.beforeSend = SentryOptions.BeforeSendCallback { event, _ ->
                if (event.isCrashed && event.throwable != null) {
                    logger.error(event.throwable!!.message, event.throwable!!)
                }

                if (event.throwable != null && event.throwable!!::class in this.config.blackList) {
                    return@BeforeSendCallback null
                }

                event
            }

            this.config.options(options)
        }
    }

    override fun intercept(e: Throwable, logger: Logger, message: String?, printStackTrace: Boolean, tags: Map<String, Any?>?) {
        fun IScope.setup() {
            clear()
            tags?.forEach { (k, v) ->
                setTag(k, v.toString())
            }

            val logId = UUID.randomUUID().toString()
            setTag("log_id", logId)
            val message = "${message ?: e.message} (${tags ?: "[]"})"

            MDC.put("log_id", logId)
            if (printStackTrace) {
                logger.error(message, e)
            } else {
                logger.error(message)
            }
        }

        if (message != null) {
            Sentry.captureMessage(message, SentryLevel.ERROR) { scope ->
                scope.setup()
            }
        } else {
            Sentry.captureException(e) { scope ->
                scope.setup()
            }
        }
    }

    override fun message(message: String, logger: Logger, tags: Map<String, Any?>?) {
        Sentry.captureMessage(message, SentryLevel.WARNING) { scope ->
            scope.clear()

            tags?.forEach { (k, v) ->
                scope.setTag(k, v.toString())
            }

            val logId = UUID.randomUUID().toString()
            scope.setTag("log_id", logId)
            val message = "$message (${tags ?: "[]"})"

            MDC.put("log_id", logId)
            logger.warn(message)
        }
    }
}
