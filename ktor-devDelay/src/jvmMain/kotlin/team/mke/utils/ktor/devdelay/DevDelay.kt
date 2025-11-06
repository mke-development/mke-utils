package team.mke.utils.ktor.devdelay

import io.ktor.server.application.*
import kotlinx.coroutines.delay
import team.mke.utils.env.env
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val devDelay by env("DEV_API_DELAY", 0.seconds) { it.toInt().milliseconds }

/**
 * Плагин для ktor, который добавляет задержку к каждому запросу.
 * Задержка может быть указана в заголовке запроса `dev-delay` в миллисекундах.
 *
 * Если заголовок не указан, используется значение по умолчанию из переменной окружения `DEV_API_DELAY` или 0,
 * если переменная не указана.
 */
val DevDelay = createRouteScopedPlugin("DevDelay") {
    onCall { call ->
        val delay = (call.request.headers["X-Dev-Delay"] ?: call.request.headers["dev-delay"])?.toLongOrNull()?.milliseconds ?: devDelay
        delay(delay)
    }
}