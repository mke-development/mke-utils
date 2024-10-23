@file:Suppress("unused")

package team.mke.utils.ktor

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.xml.*
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import team.mke.utils.env.Environment

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
    install(HttpSend) {
        maxSendCount = Int.MAX_VALUE
    }

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

internal fun Throwable.isTimeoutException(): Boolean {
    val exception = unwrapCancellationException()
    return exception is HttpRequestTimeoutException ||
            exception is ConnectTimeoutException ||
            exception is SocketTimeoutException
}

fun HttpClientConfig<*>.installHttpRequestRetry() = install(HttpRequestRetry) {
    exponentialDelay()

    retryOnServerErrors(maxRetries = Int.MAX_VALUE)
    retryOnExceptionIf(Int.MAX_VALUE) { _, cause ->
        when {
            cause.isTimeoutException() -> true
            else -> false
        }
    }
}

fun HttpRequestBuilder.acceptApplicationJson() = headers.set(HttpHeaders.Accept, ContentType.Application.Json.toString())
fun HttpRequestBuilder.acceptApplicationXml() = headers.set(HttpHeaders.Accept, ContentType.Application.Xml.toString())
fun HttpRequestBuilder.acceptProtoBuf() = headers.set(HttpHeaders.Accept, ContentType.Application.ProtoBuf.toString())
fun HttpRequestBuilder.contentTypeJson() = contentType(ContentType.Application.Json)
fun HttpRequestBuilder.contentTypeXml() = contentType(ContentType.Application.Xml)
fun HttpRequestBuilder.contentTypeProtoBuf() = contentType(ContentType.Application.ProtoBuf)

fun HttpRequestBuilder.authCircuitBreaker() = attributes.put(AuthCircuitBreaker, Unit)
