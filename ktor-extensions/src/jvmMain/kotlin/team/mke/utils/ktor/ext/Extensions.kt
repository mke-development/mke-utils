package team.mke.utils.ktor.ext

import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.RoutingCall
import io.ktor.server.util.*
import io.ktor.util.converters.*
import io.ktor.util.pipeline.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ru.raysmith.utils.nowZoned
import ru.raysmith.utils.uuid
import team.mke.utils.ktor.ErrorDTO
import java.io.File
import java.net.Inet4Address
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

val RoutingCall.ip get() = runBlocking {
    withContext(Dispatchers.IO) { Inet4Address.getByName(request.origin.remoteAddress).hostAddress }
}

fun notFound(message: String?): Nothing = throw NotFoundException(message)

/**
 * Generates a `File` object for a given `PartData.FileItem`.
 * If a file with the original name already exists, a unique identifier is appended to the name.
 *
 * @param parentPath The parent directory path where the file should be located.
 * @return A `File` object representing the file with a unique name if necessary.
 */
fun PartData.FileItem.prepareFile(parentPath: String): File {
    val path = if (parentPath.isEmpty()) "" else "${parentPath.dropLastWhile { it == '/' }}/"
    val defaultName by lazy { uuid() }
    val originalName = originalFileName ?: defaultName
    var file = File("$path$originalName")

    if (file.exists()) {
        val (name, ext) = File(originalName).let { it.nameWithoutExtension to it.extension }
        file = File("$path${name}_$defaultName.$ext")
    }

    return file
}