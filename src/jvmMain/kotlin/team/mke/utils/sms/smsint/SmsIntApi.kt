package team.mke.utils.sms.smsint

import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import team.mke.utils.sms.SmsApiException
import team.mke.utils.sms.SmsApiResult
import team.mke.utils.env.envRequired
import team.mke.utils.ktor.defaultHttpClient
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object SmsIntApi {
    private val logger = LoggerFactory.getLogger("smsint-api")
    private val token by envRequired("SMS_INT_TOKEN")

    val json = Json {
        isLenient = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val client = defaultHttpClient(OkHttp.create {
        config {
            followRedirects(true)
            connectTimeout(60.seconds.toJavaDuration())
            readTimeout(60.seconds.toJavaDuration())
        }
    }, logger) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) {
                    SmsIntApi.logger.info(message)
                }
            }
        }

        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = "lcab.smsint.ru"
                path("json", "v1.0", "")
            }
            headers.append("X-Token", token)
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }

    private suspend inline fun <reified T> request(block: () -> HttpResponse): SmsApiResult<T> {
        return try {
            val response = block()
            val text = response.readBytes().decodeToString()
            val json = json.decodeFromString<JsonObject>(text)
            if (json["error"] != null) {
                val message = json["error"]!!.jsonObject["descr"]?.jsonPrimitive?.contentOrNull
                val code = json["status_code"]?.jsonPrimitive?.intOrNull

                throw SmsApiException(message, response.status, code)
            }

            val body = SmsIntApi.json.decodeFromJsonElement<T>(json["result"]!!.jsonObject)
            SmsApiResult.Success(body)
        } catch (e: Exception) {
            SmsApiResult.Error(e)
        }
    }

    object Sms {
        suspend fun send(phone: String, message: String): SmsApiResult<SmsIntSendResponse> = request {
            client.post("sms/send/text") {
                setBody(SmsIntSendBody(listOf(Message(phone, message))))
            }
        }

        suspend fun voice(phone: String, message: String): SmsApiResult<SmsIntSendResponse> = request {
            client.post("voice/send/text") {
                setBody(SmsIntSendBody(listOf(Message(phone, message))))
            }
        }
    }
}

