package team.mke.utils.ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import io.ktor.util.converters.*
import io.ktor.util.pipeline.*
import io.ktor.util.reflect.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import ru.raysmith.utils.wrap
import ru.raysmith.utils.nowZoned
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Return deserialized an HTTP parameter or null if the parameter is not contained
 * @param param request parameter
 * */
inline fun <reified T, S : KSerializer<T>> Parameters.get(param: String, serializer: S, json: Json = team.mke.utils.ktor.json): T? {
    return get(param)?.let { if (it.isEmpty()) null else json.decodeFromString(serializer, it.wrap('"')) }
}

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

/**
 * Return deserialized an HTTP [parameter] or throw [MissingRequestParameterException] if the parameter is not contained
 * */
inline fun <reified T, S : KSerializer<T>> Parameters.getOrFail(parameter: String, serializer: S): T {
    return get(parameter, serializer) ?: throw MissingRequestParameterException(parameter)
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
    method: String = request.httpMethod.value,
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
    method: String = request.httpMethod.value,
    timestamp: ZonedDateTime = nowZoned(),
    status: HttpStatusCode = HttpStatusCode.Forbidden,
) = respondError(message ?: "Недостаточно прав", description, path, method, timestamp, status)

val PipelineContext<*, ApplicationCall>.logger get() = call.application.log