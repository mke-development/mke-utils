package team.mke.utils.ktor.ext

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.converters.*
import io.ktor.util.pipeline.*
import io.ktor.util.reflect.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

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

val PipelineContext<*, ApplicationCall>.logger get() = call.application.log

val RoutingCall.ip: String get() {
    return request.headers["X-Forwarded-For"]?.split(",")?.first()?.trim()
        ?: request.headers["X-Real-IP"]
        ?: request.origin.remoteHost
}

/**
 * Generates a `File` object for a given `PartData.FileItem` with UUID name.
 *
 * @param parentPath The parent directory path where the file should be located.
 * @param fallbackExtension
 * @return A `File` object representing the file with a unique name.
 */
fun PartData.FileItem.prepareFile(
    parentPath: String,
    fallbackExtension: String? = null,
    nextName: () -> String = { UUID.randomUUID().toString() }
): File {
    val path = if (parentPath.isEmpty()) "" else "${parentPath.dropLastWhile { it == '/' }}/"

    var name: String
    var file: File

    do {
        name = "${nextName()}${originalFileName?.substringAfterLast('.', "")?.ifEmpty { null }?.let { ".$it" } ?: fallbackExtension?.let { ".$it" } ?: ""}"
        file = File("$path$name")
    } while (file.exists())

    return file
}

/**
 * Generates a `File` object for a given `PartData.FileItem` and writes the provided byte array to it.
 * If a file with the original name already exists, a unique identifier is appended to the name.
 *
 * @param parentPath The parent directory path where the file should be located.
 * @param bytes The byte array to write to the file.
 * @return A `File` object representing the file with a unique name if necessary.
 */
fun PartData.FileItem.prepareFileAndWrite(parentPath: String, bytes: ByteArray): File = prepareFile(parentPath).apply {
    parentFile.mkdirs()
    writeBytes(bytes)
}

fun RoutingResponse.contentDispositionHeader(filename: String) {
    val parameter = if (filename.any { it.code > 127 }) {
        ContentDisposition.Parameters.FileNameAsterisk
    } else {
        ContentDisposition.Parameters.FileName
    }

    header(
        HttpHeaders.ContentDisposition,
        ContentDisposition.Attachment.withParameter(parameter, filename).toString()
    )
}
