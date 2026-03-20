package team.mke.utils.bg

import org.slf4j.Logger
import ru.raysmith.utils.today
import ru.raysmith.utils.uuid
import team.mke.utils.crashinterceptor.CrashInterceptor
import java.time.LocalDate
import java.time.LocalTime

abstract class BaseDailyBackgroundProcess(
    crashInterceptor: CrashInterceptor<*>,
    name: String,
    logger: Logger = Background.logger,
    id: String = uuid(),
) : BaseWhileBackgroundProcess(crashInterceptor, name, logger, id) {

    abstract val updateTime: LocalTime
    abstract var lastDayUpdated: LocalDate

    protected fun dailyDelay() = getDailyDelay(lastDayUpdated, updateTime)
    override fun onActionFinished() {
        super.onActionFinished()
        lastDayUpdated = today()
    }

    override fun delay() = dailyDelay()
    override fun startDelay() = dailyDelay()
}