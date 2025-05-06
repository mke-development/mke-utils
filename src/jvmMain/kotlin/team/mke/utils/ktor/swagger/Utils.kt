package team.mke.utils.ktor.swagger

import io.github.smiley4.ktorswaggerui.dsl.routes.OpenApiResponse
import io.github.smiley4.ktorswaggerui.dsl.routes.OpenApiResponses
import io.github.smiley4.ktorswaggerui.dsl.routes.OpenApiRoute
import io.ktor.http.HttpStatusCode

internal val OptionTag = "Options"

typealias OpenApiRouteBlock = OpenApiRoute.() -> Unit

fun OpenApiResponses.ok(block: OpenApiResponse.() -> Unit = {}) = HttpStatusCode.OK to {
    description = "Успех"
    block()
}

fun OpenApiResponses.created(block: OpenApiResponse.() -> Unit = {}) = HttpStatusCode.Created to {
    description = "Запись создана"
    block()
}
fun OpenApiResponses.badRequest(block: OpenApiResponse.() -> Unit = {}) = HttpStatusCode.BadRequest to {
    description = "Неверные данные"
    block()
}
fun OpenApiResponses.notFound(block: OpenApiResponse.() -> Unit = {}) = HttpStatusCode.NotFound to {
    description = "Сущность не найдена"
    block()
}
fun OpenApiResponses.locked(block: OpenApiResponse.() -> Unit = {}) = HttpStatusCode.Locked to {
    description = "Изменение заблокировано другим запросом"
    block()
}
fun OpenApiResponses.forbidden(block: OpenApiResponse.() -> Unit = {}) = HttpStatusCode.Forbidden to {
    description = "Отсутствуют необходимые права"
    block()
}
fun OpenApiResponses.noContent(block: OpenApiResponse.() -> Unit = {}) = HttpStatusCode.NoContent to {
    description = "Нет данных для ответа"
    block()
}