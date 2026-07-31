package team.mke.utils.bg

import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.Logger
import ru.raysmith.utils.uuid
import team.mke.utils.crashinterceptor.CrashInterceptor
import team.mke.utils.safe
import kotlin.time.Duration

abstract class BaseWhileBackgroundProcess(
    crashInterceptor: CrashInterceptor<*>,
    name: String,
    logger: Logger = Background.logger,
    id: String = uuid()
) : BaseBackgroundProcess(name, crashInterceptor, logger, id) {

    override val mutex: Any = object {}

    /** Максимальное количество итераций. -1 для отсутствия лимита */
    protected open val maxIterations: Int = -1

    abstract fun delay(): Duration
    abstract fun startDelay(): Duration

    abstract suspend fun action()
    open fun onActionFinished() {

    }

    override fun run(force: Boolean) {
        job = Background.scope.launch(handler + coroutineName) {
            if (!force) {
                delay(startDelay())
            }
            while (isActive) {
                synchronized(mutex) { isReadyToRestart = false }
                safe(crashInterceptor, logger) {
                    action()
                }
                safe(crashInterceptor, logger) {
                    onActionFinished()
                }
                synchronized(mutex) { isReadyToRestart = true }
                iterations++
                if (maxIterations != -1 && iterations >= maxIterations) {
                    break
                }
                delay(delay())
            }
        }
    }

    suspend fun delay(delay: Duration) {
        logger.debug("Delay for $name: {}", delay)
        kotlinx.coroutines.delay(delay)
    }
}
