package team.mke.utils.bg

import kotlinx.coroutines.launch
import org.slf4j.Logger
import ru.raysmith.utils.uuid
import team.mke.utils.crashinterceptor.CrashInterceptor

abstract class BaseBackgroundProcessImpl(
    crashInterceptor: CrashInterceptor<*>,
    name: String,
    logger: Logger = Background.logger,
    id: String = uuid()
) : BaseBackgroundProcess(name, crashInterceptor, logger, id) {

    override val mutex: Any = object {}
    abstract suspend fun action()

    override fun run(force: Boolean) {
        job = Background.scope.launch(handler + coroutineName) {
            action()
        }
    }
}
