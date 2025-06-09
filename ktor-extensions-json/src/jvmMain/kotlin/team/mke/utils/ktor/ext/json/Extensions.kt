package team.mke.utils.ktor.ext.json

import io.ktor.http.Parameters
import io.ktor.server.plugins.MissingRequestParameterException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Return deserialized an HTTP parameter or null if the parameter is not contained
 * @param param request parameter
 * */
inline fun <reified T, S : KSerializer<T>> Parameters.get(param: String, serializer: S, json: Json = team.mke.utils.json.json): T? {
    return get(param)?.let { if (it.isEmpty()) null else json.decodeFromString(serializer, "\"$it\"") }
}

/**
 * Return deserialized an HTTP [parameter] or throw [MissingRequestParameterException] if the parameter is not contained
 * */
inline fun <reified T, S : KSerializer<T>> Parameters.getOrFail(parameter: String, serializer: S): T {
    return get(parameter, serializer) ?: throw MissingRequestParameterException(parameter)
}