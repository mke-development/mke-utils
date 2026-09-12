package team.mke.utils.ktor.server.options

import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.slf4j.Logger
import ru.raysmith.utils.letIf
import team.mke.utils.crashinterceptor.CrashInterceptor
import team.mke.utils.ktor.openapi.OpenApiRouteBlock
import team.mke.utils.ktor.openapi.forbidden
import team.mke.utils.ktor.openapi.notFound
import team.mke.utils.ktor.openapi.ok
import team.mke.utils.ktor.server.ext.respondError
import team.mke.utils.model.ErrorDTO
import team.mke.utils.model.OptionValue
import team.mke.utils.safe

class OptionsPluginConfiguration {
    var path = "/options"
    var json: Json = team.mke.utils.json.json
    var crashInterceptor: CrashInterceptor<*>? = null
    var logger: Logger? = null

    companion object {
        internal val verificationKeysWhiteListKey = "verificationKeysWhiteList"
    }

    var getDocsSetup: RouteConfig.() -> Unit = {}
    var getDocs: OpenApiRouteBlock = {
        description = "Возвращает запрошенные настройки"
        tags(OptionTag)

        request {
            queryParameter<List<String>>("keys") {
                required = true
                example("Список ключей") {
                    value = listOf("foo", "bar")
                }
            }
        }

        response {
            ok {
                body<Map<String, OptionValue<Any?>>> {
//                    example("Список настроек") { // TODO to fix that provide Json builder (with OptionValue<Any?> serializer) for ExampleEncoder
//                        value = mapOf("foo" to OptionValue(1), "bar" to OptionValue(true))
//                    }
                }
            }
            notFound {
                description = "Ключ не найден"
            }
            forbidden {
                description = "Отсутствуют права доступа к одному или нескольким ключам"
            }
        }

        getDocsSetup()
    }

    var putDocsSetup: RouteConfig.() -> Unit = {}
    val putDocs: OpenApiRouteBlock = {
        description = buildString {
            append("Устанавливает настройки.")
        }
        tags(OptionTag)

        request {
            body<Map<String, Any?>> {
                required = true
                description = """
                    Список ключей и значений. Дополнительно можно передать:
                    - `${verificationKeysWhiteListKey}` - Список ключей по которым будет выполнен триггер на валидацию. По умолчанию — все ключи.
                    Пример: в настройке интеграции, когда есть ключи login и password и идентичные триггеры с валидацией подключения, 
                    при обновлении данных, сначала будет произведена попытка авторизации с новым логином и старым паролем, что приведет к ошибке.
                    При указании `${verificationKeysWhiteListKey}=password`, триггер будет вызван только один раз после сохранения нового логина. 
                """.trimIndent()
//                example("Список ключей") { // TODO to fix that provide Json builder (with Map<String, Any?> serializer) for ExampleEncoder
//                    value = mapOf("foo" to 2, "bar" to false)
//                }
            }
        }

        response {
            ok {
                body<Map<String, OptionValue<Any?>>> {
//                    example("Список измененных настроек") { // TODO to fix that provide Json builder (with OptionValue<Any?> serializer) for ExampleEncoder
//                        value = mapOf("foo" to OptionValue(1), "bar" to OptionValue(true))
//                    }
                }
            }
            notFound {
                description = "Ключ не найден"
            }
            forbidden {
                description = "Отсутствуют права доступа к одному или нескольким ключам"
            }
        }

        putDocsSetup()
    }

    internal var configuration: context(OptionsPluginConfiguration, Route) OptionsRoutingContext.() -> Unit = {}

    fun routes(block: context(OptionsPluginConfiguration, Route) OptionsRoutingContext.() -> Unit) {
        configuration = block
    }
}

fun Route.configureOptions(configuration: OptionsPluginConfiguration.() -> Unit = {}) {
    val config = OptionsPluginConfiguration().apply(configuration)
    check(config.crashInterceptor != null) { "CrashInterceptor should be provided" }

    route(config.path) {
        fun RoutingContext.errorDTO(key: String, method: HttpMethod) = ErrorDTO(
            description = "Key '$key' not found in options",
            path = call.request.path() + config.path.letIf({ !it.startsWith("/") }) { "/$it" },
            method = method
        )

        val context = OptionsRoutingContext()
        config.configuration.invoke(config, this, context)

        get(config.getDocs) {
            val keys = call.parameters.getAll("keys") ?: emptyList()

            val result = mutableMapOf<String, JsonElement>()
            for (key in keys) {
                val handler = OptionHandler.registered[key] ?: run {
                    call.respond(HttpStatusCode.NotFound, errorDTO(key, HttpMethod.Get))
                    return@get
                }
                if (handler.checkAccess != null && !handler.checkAccess.invoke(call)) {
                    call.respondError(
                        message = "Доступ запрещен",
                        description = "Access denied for option '$key'",
                        status = HttpStatusCode.Forbidden
                    )
                    return@get
                }
                result[key] = handler.encodeToJsonElement()
            }

            call.respond(JsonObject(result))
        }

        put(config.putDocs) {
            val text = call.receiveText()
            val verificationKeysWhiteList = mutableListOf<String>()
            val options = config.json.decodeFromString<JsonObject>(text).map { (key, value) ->
                when (key) {
                    OptionsPluginConfiguration.verificationKeysWhiteListKey -> {
                        check(value is JsonArray) { "$key should be json array" }
                        verificationKeysWhiteList.addAll(value.map {
                            check(it is JsonPrimitive) { "${key}'s entries should be primitive" }
                            it.content
                        })
                        null
                    }
                    else -> {
                        OptionHandler.registered[key]?.let { handler ->
                            key to if (value is JsonNull) null else handler.decodeFromJsonElement(value)
                        } ?: run {
                            call.respond(HttpStatusCode.NotFound, errorDTO(key, HttpMethod.Put))
                            return@put
                        }
                    }
                }
            }.filterNotNull().toMap()

            for ((key, _) in options) {
                val handler = OptionHandler.registered[key]!!
                if (handler.checkAccess != null && !handler.checkAccess.invoke(call)) {
                    call.respondError(
                        message = "Доступ запрещен",
                        description = "Access denied for option '$key'",
                        status = HttpStatusCode.Forbidden
                    )
                    return@put
                }
            }

            val rollbackData = options.map { (key, _) ->
                key to OptionHandler.registered[key]!!.get().value
            }.toMap()

            try {
                val updated = mutableMapOf<String, Any?>()

                val result = JsonObject(
                    options.map { (key, newValue) ->
                        OptionHandler.registered[key]!!.let { handler ->
                            val shouldBeVerified = verificationKeysWhiteList.isEmpty() || verificationKeysWhiteList.contains(key)
                            if (handler.setValue(newValue, shouldBeVerified = shouldBeVerified)) {
                                updated[key] = newValue
                            }
                            key to handler.encodeToJsonElement()
                        }
                    }.toMap()
                )

                safe(config.crashInterceptor!!, config.logger ?: environment.log) {
                    val updates = mutableListOf<suspend (newValue: Any?) -> Unit>()
                    updated.forEach { (key, newValue) ->
                        OptionHandler.registered[key]?.let { handler ->
                            if (handler.onUpdate !in updates) {
                                updates.add(handler.onUpdate)
                                handler.onUpdate(newValue)
                            }
                        }
                    }
                }

                call.respond(result)
            } catch(e: Exception) {
                safe(config.crashInterceptor!!, config.logger ?: environment.log) {
                    rollbackData.forEach { (key, value) ->
                        OptionHandler.registered[key]?.setValue(value, shouldBeVerified = false)
                    }
                }
                throw e
            }
        }
    }
}
