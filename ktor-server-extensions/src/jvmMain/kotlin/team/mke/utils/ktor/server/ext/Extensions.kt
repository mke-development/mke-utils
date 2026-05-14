package team.mke.utils.ktor.server.ext

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import ru.raysmith.utils.nowZoned
import team.mke.utils.model.ErrorDTO
import team.mke.utils.model.Sortable
import java.time.ZonedDateTime
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

suspend fun ApplicationCall.respondError(
    message: String,
    description: String? = null,
    path: String = request.path(),
    method: HttpMethod = request.httpMethod,
    timestamp: ZonedDateTime = nowZoned(),
    status: HttpStatusCode = HttpStatusCode.BadRequest,
) = respond(status, ErrorDTO(message, description, path, method, timestamp))

suspend fun ApplicationCall.forbidden(
    message: String,
    description: String? = null,
    path: String = request.path(),
    method: HttpMethod = request.httpMethod,
    timestamp: ZonedDateTime = nowZoned(),
    status: HttpStatusCode = HttpStatusCode.Forbidden,
) = respondError(message, description, path, method, timestamp, status)

suspend fun ApplicationCall.respondWrongSort(field: String, dto: KClass<*>) = respondWrongSort(
    field, dto.declaredMemberProperties.filter { !it.hasAnnotation<Sortable>() }.map { it.name }
)

suspend fun ApplicationCall.respondWrongSort(field: String, allowedFields: List<String>) = respondError(
    message = "Sorting by field '$field' is not possible. Allowed fields for sorting: ${allowedFields.joinToString(", ")}",
)
