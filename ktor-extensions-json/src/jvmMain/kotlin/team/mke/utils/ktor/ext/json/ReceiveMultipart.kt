package team.mke.utils.ktor.ext.json

import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.set
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.formFieldLimit
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.routing.RoutingResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import java.util.UUID
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

data class FilePartData(
    val part: PartData.FileItem,
    val bytes: ByteArray,
    val mimeType: String? = part.mimeType()
) {
    private val id = UUID.randomUUID()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilePartData

        if (part != other.part) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = part.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }
}

suspend inline fun <reified DTO : Any> ApplicationCall.receiveMultipart(
    json: Json,
    formFieldLimit: Long = -1L,
    crossinline onFileItem: suspend (part: PartData.FileItem) -> FilePartData?
): MultipartRequestData<DTO> {
    if (formFieldLimit > 0) {
        this.formFieldLimit = formFieldLimit
    }

    val expectedDTOFields = DTO::class.memberProperties
        .filter { it.visibility == KVisibility.PUBLIC }
        .map { it.findAnnotation<SerialName>()?.value ?: it.name }

    val files = mutableListOf<FilePartData>()
    val dtoFields = mutableMapOf<String, Any>()
    val otherFields = mutableMapOf<PartData.FormItem, Any>()

    receive<MultiPartData>().forEachPart { part ->
        when(part) {
            is PartData.FileItem -> {
                val data = onFileItem(part)
                if (data != null) {
                    files.add(data)
                }
            }
            is PartData.FormItem -> {
                if (part.name != null) {
                    if (part.name in expectedDTOFields) {
                        dtoFields[part.name!!] = part.value
                    } else {
                        otherFields[part] = part.value
                    }
                }
            }
            is PartData.BinaryChannelItem -> {}
            is PartData.BinaryItem -> {}
        }

        part.dispose()
    }

    val dto = buildJsonObject {
        dtoFields.forEach { (key, value) ->
            when (value) {
                is String if value.startsWith("{") && value.endsWith("}") -> {
                    put(key, json.decodeFromString<JsonObject>(value))
                }
                is String if value.startsWith("[") && value.endsWith("]") -> {
                    put(key, json.decodeFromString<JsonArray>(value))
                }
                is String -> put(key, JsonPrimitive(value))
                is Number -> put(key, JsonPrimitive(value))
                is Boolean -> put(key, JsonPrimitive(value))
                else -> put(key, JsonPrimitive(value.toString()))
            }
        }
    }.let {
        json.decodeFromJsonElement<DTO>(it)
    }

    return MultipartRequestData(dto, files, otherFields)
}

data class MultipartRequestData<T>(
    val dto: T,
    val files: List<FilePartData>,
    val otherFields: Map<PartData.FormItem, Any>
)

fun PartData.FileItem.mimeType() = headers[HttpHeaders.ContentType]