package team.mke.utils.ktor.ext

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import io.ktor.util.converters.*
import io.ktor.util.pipeline.*
import io.ktor.util.reflect.*
import ru.raysmith.utils.nowZoned
import team.mke.utils.ktor.ErrorDTO
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified T> Parameters.get(param: String): T? {
    val typeInfo = typeInfo<T>()
    val values = getAll(param) ?: return null
    return try {
        DefaultConversionService.fromValues(values, typeInfo) as T
    } catch (cause: Exception) {
        throw ParameterConversionException(param, typeInfo.type.simpleName ?: typeInfo.type.toString(), cause)
    }
}

fun Parameters.dateOrFail(
    name: String = "date", formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
): LocalDate = getOrFail(name).let { LocalDate.parse(it, formatter) }

fun Parameters.date(
    name: String = "date", formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
) = get(name)?.let { LocalDate.parse(it, formatter) }

suspend fun ApplicationCall.respondError(
    message: String? = null,
    description: String? = null,
    path: String = request.path(),
    method: HttpMethod = request.httpMethod,
    timestamp: ZonedDateTime = nowZoned(),
    status: HttpStatusCode = HttpStatusCode.BadRequest,
): ErrorDTO {
    return ErrorDTO(message ?: "Ошибка", description, path, method, timestamp).also {
        respond(status, it)
    }
}

suspend fun ApplicationCall.forbidden(
    message: String? = null,
    description: String? = null,
    path: String = request.path(),
    method: HttpMethod = request.httpMethod,
    timestamp: ZonedDateTime = nowZoned(),
    status: HttpStatusCode = HttpStatusCode.Forbidden,
) = respondError(message ?: "Недостаточно прав", description, path, method, timestamp, status)

val PipelineContext<*, ApplicationCall>.logger get() = call.application.log