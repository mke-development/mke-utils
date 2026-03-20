@file:Suppress("unused")

package team.mke.utils.ktor.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.utils.*
import io.ktor.http.*
import team.mke.utils.env.Environment

// TODO docs; describe about missed ContentNegotiation
fun defaultHttpClient(
    engine: HttpClientEngine,
    logger: org.slf4j.Logger,

    loggingConfig: (LoggingConfig.() -> Unit)? = {},
    httpRequestRetryConfig: (HttpRequestRetryConfig.() -> Unit)? = {},
    httpSendConfig: (HttpSend.Config.() -> Unit)? = {},

    block: HttpClientConfig<*>.() -> Unit = {}
) = HttpClient(engine) {
    expectSuccess = true

    if (loggingConfig != null) {
        install(Logging, loggingConfig)
    }
    if (httpRequestRetryConfig != null) {
        installHttpRequestRetry(httpRequestRetryConfig)
    }

    if (httpSendConfig != null) {
        install(HttpSend) {
            maxSendCount = Int.MAX_VALUE
            httpSendConfig()
        }
    }

    block(this)
}

fun HttpClientConfig<*>.installLogging(logger: org.slf4j.Logger, loggingConfig: LoggingConfig.() -> Unit) = install(Logging) {
    this.level = LogLevel.INFO
    this.logger = object : Logger {
        override fun log(message: String) {
            logger.debug(message)
        }
    }
    loggingConfig()
}

internal fun Throwable.isTimeoutException(): Boolean {
    val exception = unwrapCancellationException()
    return exception is HttpRequestTimeoutException ||
            exception is ConnectTimeoutException ||
            exception is SocketTimeoutException
}

fun HttpClientConfig<*>.installHttpRequestRetry(httpRequestRetryConfig: HttpRequestRetryConfig.() -> Unit) = install(HttpRequestRetry) {
    exponentialDelay()

    retryOnServerErrors(maxRetries = Int.MAX_VALUE - 1) // -1 because HttpSend adds one more retry
    retryOnExceptionIf(Int.MAX_VALUE - 1) { _, cause ->
        when {
            cause.isTimeoutException() -> true
            else -> false
        }
    }

    httpRequestRetryConfig()
}

fun HttpRequestBuilder.acceptApplicationJson() = headers.set(HttpHeaders.Accept, ContentType.Application.Json.toString())
fun HttpRequestBuilder.acceptApplicationXml() = headers.set(HttpHeaders.Accept, ContentType.Application.Xml.toString())
fun HttpRequestBuilder.acceptProtoBuf() = headers.set(HttpHeaders.Accept, ContentType.Application.ProtoBuf.toString())
fun HttpRequestBuilder.contentTypeJson() = contentType(ContentType.Application.Json)
fun HttpRequestBuilder.contentTypeXml() = contentType(ContentType.Application.Xml)
fun HttpRequestBuilder.contentTypeProtoBuf() = contentType(ContentType.Application.ProtoBuf)

fun HttpRequestBuilder.authCircuitBreaker() = attributes.put(AuthCircuitBreaker, Unit)
