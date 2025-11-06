package team.mke.utils.ktor.ext.json

import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.server.plugins.MissingRequestParameterException
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.serializer
import ru.raysmith.utils.forEachLet
import kotlin.reflect.full.isSubclassOf

/**
 * Return deserialized an HTTP parameter or null if the parameter is not contained
 * @param param request parameter
 * @param json Json instance to use for deserialization
 * */
inline fun <reified T, S : KSerializer<T>> Parameters.get(
    param: String,
    serializer: S,
    json: Json = team.mke.utils.json.json,
    parseAsString: Boolean = T::class.isSubclassOf(CharSequence::class)
): T? {
    return get(param)?.let { if (it.isEmpty()) null else json.decodeFromString(serializer, if (parseAsString) "\"$it\"" else it) }
}

/**
 * Return deserialized an HTTP parameter or null if the parameter is not contained
 * @param param request parameter
 * @param json Json instance to use for deserialization
 * */
inline fun <reified T> Parameters.get(
    param: String,
    json: Json = team.mke.utils.json.json,
    parseAsString: Boolean = T::class.isSubclassOf(CharSequence::class)
): T? {
    return get(param)?.let { if (it.isEmpty()) null else json.decodeFromString(if (parseAsString) "\"$it\"" else it) }
}

/**
 * Return deserialized an HTTP [parameter] or throw [MissingRequestParameterException] if the parameter is not contained
 * */
inline fun <reified T, S : KSerializer<T>> Parameters.getOrFail(parameter: String, serializer: S): T {
    return get(parameter, serializer) ?: throw MissingRequestParameterException(parameter)
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
@JvmName("getAllJson")
inline fun <reified T> Parameters.getAll(name: String, json: Json = team.mke.utils.json.json) = getAll(name)
    ?.map { arr ->
        arr.split(",").map {
            json.decodeFromString<T>(it.trim())
        }
    }?.flatten()

fun List<PartData>.toJson() = buildJsonObject {
    this@toJson.filter { it is PartData.FormItem && it.name != null }.forEach {
        require(it is PartData.FormItem)
        if (it.value.startsWith("[") && it.value.endsWith("]")) {
            putJsonArray(it.name!!) {
                it.value.drop(1).dropLast(1).split(",").forEachLet({ s -> s.trim() }) { element ->
                    if (element.startsWith("\"") && element.endsWith("\"")) {
                        add(JsonPrimitive(element.drop(1).dropLast(1)))
                    } else if (element.isNotEmpty()) {
                        add(JsonPrimitive(element))
                    }
                }
            }
        } else {
            put(it.name!!, JsonPrimitive(it.value))
        }
    }
}