@file:Suppress("unused")

package team.mke.utils.ktor

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.xml.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.modules.SerializersModule
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import ru.raysmith.utils.safe
import team.mke.utils.env.Environment
import team.mke.utils.serialization.BigDecimalSerializer
import team.mke.utils.serialization.KotlinxLocalDateSerializer
import team.mke.utils.serialization.LocalDateSerializer
import team.mke.utils.serialization.ZonedDateTimeSerializer
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import io.ktor.serialization.kotlinx.json.json
import java.lang.Exception

// --------------------------------------------------- JSON ------------------------------------------------------------

/**
 * Creates an instance of Json configured from the optionally given Json instance and adjusted with builderAction.
 *
 * Создает экземпляр [Json] используя настройки [from] -> [applyDefaultJson] -> [builderAction]
 * */
fun json(from: Json, builderAction: JsonBuilder.() -> Unit) = Json(from) {
    applyDefaultJson()
    builderAction()
}

/**
 * [Json] по умолчанию
 *
 * @see applyDefaultJson
 * */
val json = Json {
    applyDefaultJson()
}

/**
 * Устанавливает данные по умолчанию
 *
 * ```
 * isLenient = true
 * ignoreUnknownKeys = true
 * prettyPrint = Environment.isDev()
 * serializersModule = SerializersModule {
 *     contextual(LocalDate::class, LocalDateSerializer)
 *     contextual(ZonedDateTime::class, ZonedDateTimeSerializer)
 *     contextual(BigDecimal::class, BigDecimalSerializer)
 *     safe {
 *         Class.forName("kotlinx.datetime.LocalDate")
 *         contextual(kotlinx.datetime.LocalDate::class, KotlinxLocalDateSerializer)
 *     }
 * }
 * ```
 * */
private fun JsonBuilder.applyDefaultJson() {
    isLenient = true
    ignoreUnknownKeys = true
    prettyPrint = Environment.isDev()
    serializersModule = SerializersModule {
        contextual(LocalDate::class, LocalDateSerializer)
        contextual(ZonedDateTime::class, ZonedDateTimeSerializer)
        contextual(BigDecimal::class, BigDecimalSerializer)
        safe {
            Class.forName("kotlinx.datetime.LocalDate")
            contextual(kotlinx.datetime.LocalDate::class, KotlinxLocalDateSerializer)
        }
    }
}

// -------------------------------------------------- XML --------------------------------------------------------------

/** [XML] по умолчанию */
@OptIn(ExperimentalXmlUtilApi::class)
val xml = XML {
    xmlDeclMode = XmlDeclMode.Charset
    xmlVersion = XmlVersion.XML10
    defaultPolicy {
        autoPolymorphic = true
        unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
    }
}

// ------------------------------------------------- Client ------------------------------------------------------------

fun defaultHttpClient(
    engine: HttpClientEngine,
    logger: org.slf4j.Logger,
    logLevel: LogLevel = LogLevel.INFO,
    json: Json? = team.mke.utils.ktor.json,
    xml: XML? = null,
    block: HttpClientConfig<*>.() -> Unit = {}
) = HttpClient(engine) {
    expectSuccess = true
    developmentMode = Environment.isDev()
    installLogging(logger, logLevel)
    if (json != null || xml != null) {
        installContentNegotiation(json, xml)
    }
    installHttpRequestRetry()

    block(this)
}

fun HttpClientConfig<*>.installLogging(logger: org.slf4j.Logger, level: LogLevel = LogLevel.INFO) = install(Logging) {
    this.level = level
    this.logger = object : Logger {
        override fun log(message: String) {
            logger.debug(message)
        }
    }
}

fun HttpClientConfig<*>.installContentNegotiation(json: Json?, xml: XML?) = install(ContentNegotiation) {
    if (json != null) {
        json(json)
    }
    if (xml != null) {
        xml(xml)
    }
}

fun HttpClientConfig<*>.installHttpRequestRetry() = install(HttpRequestRetry) {
    retryOnException(retryOnTimeout = true)
    retryOnServerErrors(maxRetries = Int.MAX_VALUE)
    exponentialDelay()
}

fun HttpRequestBuilder.acceptApplicationJson() = headers.set(HttpHeaders.Accept, ContentType.Application.Json.toString())
fun HttpRequestBuilder.acceptApplicationXml() = headers.set(HttpHeaders.Accept, ContentType.Application.Xml.toString())
fun HttpRequestBuilder.acceptProtoBuf() = headers.set(HttpHeaders.Accept, ContentType.Application.ProtoBuf.toString())
fun HttpRequestBuilder.contentTypeJson() = contentType(ContentType.Application.Json)
fun HttpRequestBuilder.contentTypeXml() = contentType(ContentType.Application.Xml)
fun HttpRequestBuilder.contentTypeProtoBuf() = contentType(ContentType.Application.ProtoBuf)

fun HttpRequestBuilder.authCircuitBreaker() = attributes.put(Auth.AuthCircuitBreaker, Unit)
