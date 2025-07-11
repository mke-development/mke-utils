package team.mke.utils.ktor.ext.xml

import io.ktor.http.*
import io.ktor.server.plugins.MissingRequestParameterException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML

/**
 * Return deserialized an HTTP parameter or null if the parameter is not contained
 * @param param request parameter
 * */
inline fun <reified T, S : KSerializer<T>> Parameters.get(param: String, serializer: S, xml: XML = team.mke.utils.xml.xml): T? {
    return get(param)?.let { if (it.isEmpty()) null else xml.decodeFromString(serializer, "\"$it\"") }
}

/**
 * Return deserialized an HTTP [parameter] or throw [MissingRequestParameterException] if the parameter is not contained
 * */
inline fun <reified T, S : KSerializer<T>> Parameters.getOrFail(parameter: String, serializer: S): T {
    return get(parameter, serializer) ?: throw MissingRequestParameterException(parameter)
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
@JvmName("getAllXml")
inline fun <reified T> Parameters.getAll(name: String, xml: XML = team.mke.utils.xml.xml) = getAll(name)
    ?.map { arr ->
        arr.split(",").map {
            xml.decodeFromString<T>(it.trim())
        }
    }?.flatten()