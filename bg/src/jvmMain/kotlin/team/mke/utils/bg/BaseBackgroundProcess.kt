package team.mke.utils.bg

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.raysmith.utils.uuid
import team.mke.utils.crashinterceptor.CrashInterceptor
import team.mke.utils.logging.LoggableCoroutineName

/**
 * Базовая обертка для фонового процесса
 *
 * @param name Имя процесса
 * @param id уникальный идентификатор
 * */
abstract class BaseBackgroundProcess(
    final override val name: String,
    val crashInterceptor: CrashInterceptor<*>,
    override val logger: Logger = LoggerFactory.getLogger("bg"),
    override val id: String = uuid(),
) : BackgroundProcess {
    protected var job: Job? = null
    override val isActive get() = job?.isActive == true

    protected open var isReadyToRestart = true
    protected var restartOnFinish = false

    var iterations = 0
        protected set

    abstract val mutex: Any

    val coroutineName = LoggableCoroutineName(name, threadName = "bg")
    val handler = CoroutineExceptionHandler { _, throwable ->
        crashInterceptor.intercept(
            throwable,
            logger,
            "Error in background process '$name' [$id]. Process stopped."
        )
        Background.removeProcess(id)
    }

    suspend fun join() {
        job?.join()
    }

    override fun cancel(cause: CancellationException?) {
        job?.cancel(cause)
    }
    suspend fun cancelAndJoin() { job?.cancelAndJoin() }

    fun start(force: Boolean = false) = start(true, force)
    override fun start(throwOnRegistered: Boolean, force: Boolean) {
        Thread.currentThread().setName("bg")
        logger.debug("Background process '$name' [$id] started...")
        Background.registered(this, throwOnRegistered)
        synchronized(mutex) {
            isReadyToRestart = false
        }
        job = Background.scope.launch(handler + coroutineName) {
            try {
                run(force)
                logger.debug("Background process '$name' [$id] completed")
            } catch (e: Exception) {
                crashInterceptor.intercept(e, logger, "Не удалось запустить фоновый процесс '$name' [$id]")
            } finally {
                synchronized(mutex) {
                    if (restartOnFinish) {
                        restart()
                    } else {
                        isReadyToRestart = true
                    }
                }
                if (iterations == 0) iterations++
            }
        }
    }

    override fun restart(force: Boolean) = synchronized(mutex) {
        if (isReadyToRestart) {
            isReadyToRestart = false
            restartOnFinish = false
            super.restart(force)
            return@synchronized true
        } else {
            restartOnFinish = true
            return@synchronized false
        }
    }
}

