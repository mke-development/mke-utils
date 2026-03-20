package team.mke.utils.ktor.devplugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import team.mke.utils.model.ErrorDTO

/**
 * Плагин для ktor, который позволяет замокировать статус ответа.
 * Статус может быть указан в заголовке запроса `X-Mock-Status` в виде числового кода HTTP статуса.
 *
 * Если заголовок не указан или содержит некорректное значение, статус ответа не изменяется.
 * */
val MockStatus = createRouteScopedPlugin("MockStatus", { MockStatusConfig() }) {
    onCall { call ->
        val mockStatusValue = call.request.headers[HttpHeaders.XMockStatus]?.toIntOrNull() ?: return@onCall
        val status = HttpStatusCode.fromValue(mockStatusValue)

        if (!pluginConfig.response(status, call)) {
            call.respond(status)
        }
    }
}

class MockStatusConfig {
    internal var response: suspend (status: HttpStatusCode, ApplicationCall) -> Boolean = f@ { status, call ->
        if (status.value >= 400) {
            call.respond(status, ErrorDTO(
                message = "${status.description} (mocked)",
                description = null,
                path = call.request.path(),
                method = call.request.httpMethod,
            ))

            return@f true
        }
        false
    }

    fun response(block: suspend (status: HttpStatusCode, ApplicationCall) -> Boolean) {
        response = block
    }
}

val HttpHeaders.XMockStatus: String get() = "X-Mock-Status"
