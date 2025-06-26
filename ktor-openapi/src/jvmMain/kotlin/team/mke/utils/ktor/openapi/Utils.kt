package team.mke.utils.ktor.openapi

import io.github.smiley4.ktoropenapi.config.ResponseConfig
import io.github.smiley4.ktoropenapi.config.ResponsesConfig
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.ktor.http.HttpStatusCode

typealias OpenApiRouteBlock = RouteConfig.() -> Unit

fun ResponsesConfig.ok(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.OK to {
    description = "Успех"
    block()
}

fun ResponsesConfig.created(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.Created to {
    description = "Запись создана"
    block()
}

fun ResponsesConfig.badRequest(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.BadRequest to {
    description = "Неверные данные"
    block()
}
fun ResponsesConfig.notFound(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.NotFound to {
    description = "Сущность не найдена"
    block()
}
fun ResponsesConfig.locked(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.Locked to {
    description = "Изменение заблокировано другим запросом"
    block()
}
fun ResponsesConfig.forbidden(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.Forbidden to {
    description = "Отсутствуют необходимые права"
    block()
}
fun ResponsesConfig.noContent(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.NoContent to {
    description = "Нет данных для ответа"
    block()
}
fun ResponsesConfig.unauthorized(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.Unauthorized to {
    description = "Авторизация не пройдена"
    block()
}