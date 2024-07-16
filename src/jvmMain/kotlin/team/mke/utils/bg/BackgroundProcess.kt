package team.mke.utils.bg

import org.slf4j.Logger
import kotlin.coroutines.cancellation.CancellationException

/**
 * Фоновый процесс.
 * Должен быть зарегистрирован при создании и удален для освобождения из списка процессов в [Background]
 * */
interface BackgroundProcess {
    val id: String
    val name: String
    val isActive: Boolean
    val logger: Logger

    fun run()
    fun cancel(cause: CancellationException? = null)
    fun onCancel() {

    }

    /** Перезапускает процесс. Возвращает true если был перезапущен, false если перезапуск был отложен */
    fun restart(): Boolean {
        logger.debug("Background process '$name' restarted...")
        Background.restart(id)
        return true
    }

    /** Регистрирует и запускает процесс */
    fun start(throwOnRegistered: Boolean) {
        logger.debug("Background process '$name' started...")
        Background.registered(this, throwOnRegistered)
        run()
    }

    /** Останавливает и удаляет процесс */
    fun stop(cause: CancellationException? = null) {
        logger.debug("Background process '$name' stopped...")
        cancel(cause)
        onCancel()
        Background.removeProcess(id)
    }
}

