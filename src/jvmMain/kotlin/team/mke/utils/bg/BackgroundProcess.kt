package team.mke.utils.bg

/**
 * Фоновый процесс.
 * Должен быть зарегистрирован при создании и удален для освобождения из списка процессов в [Background]
 * */
interface BackgroundProcess {
    val id: String
    val name: String
    val isActive: Boolean

    fun run()
    fun cancel()
    fun onCancel() {

    }

    /** Перезапускает процесс. Возвращает true если был перезапущен, false если перезапуск был отложен */
    fun restart(): Boolean {
        Background.logger.debug("Background process '$name' restarted...")
        Background.restart(id)
        return true
    }

    /** Регистрирует и запускает процесс */
    fun start(throwOnRegistered: Boolean) {
        Background.logger.debug("Background process '$name' started...")
        Background.registered(this, throwOnRegistered)
        run()
    }

    /** Останавливает и удаляет процесс */
    fun stop() {
        Background.logger.debug("Background process '$name' stopped...")
        cancel()
        onCancel()
        Background.removeProcess(id)
    }
}

