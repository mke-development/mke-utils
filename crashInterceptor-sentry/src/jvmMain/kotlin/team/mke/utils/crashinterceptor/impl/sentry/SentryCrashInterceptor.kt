package team.mke.utils.crashinterceptor.impl.sentry

import io.sentry.IScope
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import org.slf4j.Logger
import team.mke.utils.env.Environment
import team.mke.utils.env.env
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass
import team.mke.utils.crashinterceptor.CrashInterceptor
import team.mke.utils.crashinterceptor.CrashInterceptorConfig
import java.util.UUID

object SentryCrashInterceptor : CrashInterceptor<SentryCrashInterceptor.Config> {

    class Config : CrashInterceptorConfig {

        var dsn: String? = null

        private val _blackList: MutableSet<KClass<out Exception>> = mutableSetOf(SocketTimeoutException::class, SocketException::class)
        val blackList: Set<KClass<out Exception>>
            get () = _blackList


        fun blackList(vararg classes: KClass<out Exception>) {
            this._blackList.addAll(classes)
        }

        internal var options: SentryOptions.() -> Unit = {}
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
            options.isDebug = Environment.Companion.isDev()
            options.environment = env.name.lowercase()
            options.isEnableAutoSessionTracking = false
            options.isEnableUncaughtExceptionHandler = true

            options.beforeSend = SentryOptions.BeforeSendCallback { event, _ ->
                if (event.isCrashed && event.throwable != null) {
                    logger.error(event.throwable!!.message, event.throwable!!)
                }

                if (Environment.Companion.isDev()) {
                    return@BeforeSendCallback null
                }

                if (event.throwable != null && event.throwable!!::class in this.config.blackList) {
                    return@BeforeSendCallback null
                }

                if (event.throwable is CancellationException) {
                    return@BeforeSendCallback null
                }

                event
            }

            this.config.options(options)
        }
    }

    override fun intercept(e: Throwable, logger: Logger, message: String?, tags: Map<String, Any?>?) {
        fun IScope.setup() {
            clear()
            tags?.forEach { (k, v) ->
                setTag(k, v.toString())
            }

            val logId = UUID.randomUUID().toString()
            setTag("log_id", logId)
            logger.error("[$logId] ${message ?: e.message} (${tags ?: "[]"})", e)
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

            logger.warn("$message (${tags ?: "[]"})")
        }
    }
}