package team.mke.utils.ktor.server.options

import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.slf4j.Logger
import ru.raysmith.utils.letIf
import team.mke.utils.crashinterceptor.CrashInterceptor
import team.mke.utils.ktor.server.options.OptionsRoutingContext.Companion.sets
import team.mke.utils.ktor.openapi.OpenApiRouteBlock
import team.mke.utils.ktor.openapi.notFound
import team.mke.utils.ktor.openapi.ok
import team.mke.utils.model.ErrorDTO
import team.mke.utils.model.OptionValue
import team.mke.utils.safe

class OptionsPluginConfiguration {
    var path = "/options"
    var json: Json = team.mke.utils.json.json
    var crashInterceptor: CrashInterceptor<*>? = null
    var logger: Logger? = null

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
                body<Map<String, OptionValue<*>>> {
                    example("Список настроек") {
                        value = mapOf("foo" to OptionValue(1), "bar" to OptionValue(true))
                    }
                }
            }
            notFound {
                description = "Ключ не найден"
            }
        }

        getDocsSetup()
    }

    var putDocsSetup: RouteConfig.() -> Unit = {}
    val putDocs: OpenApiRouteBlock = {
        description = buildString {
            append("Устанавливает настройки. Допускает только следующие списки:")
            sets.keys.forEach { options ->
                append(options.joinToString("`, `", prefix = "\n- `", postfix = "`\n"))
            }
        }
        tags(OptionTag)

        request {
            body<Map<String, Any?>> {
                required = true
                example("Список ключей") {
                    value = mapOf("foo" to 2, "bar" to false)
                }
            }
        }

        response {
            ok {
                body<Map<String, OptionValue<*>>> {
                    example("Список измененных настроек") {
                        value = mapOf("foo" to OptionValue(1), "bar" to OptionValue(true))
                    }
                }
            }
            notFound {
                description = "Ключ не найден"
            }
        }

        putDocsSetup()
    }

    internal var configuration: context(OptionsPluginConfiguration, OptionsRoutingContext, Route) () -> Unit = {}

    fun routes(block: context(OptionsPluginConfiguration, OptionsRoutingContext, Route) () -> Unit) {
        configuration = block
    }
}

fun Route.configureOptions(configuration: OptionsPluginConfiguration.() -> Unit) {
    val config = OptionsPluginConfiguration().apply(configuration)

    route(config.path) {
        fun RoutingContext.errorDTO(key: String, method: HttpMethod) = ErrorDTO(
            description = "Key '$key' not found in options",
            path = call.request.path() + config.path.letIf({ !it.startsWith("/") }) { "/$it" },
            method = method
        )

        val context = OptionsRoutingContext()
        config.configuration(config, context, this)

        get(config.getDocs) {
            val keys = call.parameters.getOrFail("keys").split(",")

            call.respond(
                JsonObject(
                    keys.map { key ->
                        OptionHandler.registered[key]?.let { handler ->
                            key to handler.encodeToJsonElement()
                        } ?: run {
                            call.respond(HttpStatusCode.NotFound, errorDTO(key, HttpMethod.Get))
                            return@get
                        }
                    }.toMap()
                )
            )
        }

        put(config.putDocs) {
            val text = call.receiveText()
            val options = config.json.decodeFromString<JsonObject>(text).map { (key, value) ->
                check(value is JsonPrimitive) { "Option value should be primitive" }
                OptionHandler.registered[key]?.let { handler ->
                    key to if (value is JsonNull) null else handler.decodeFromJsonElement(value)
                } ?: run {
                    call.respond(HttpStatusCode.NotFound, errorDTO(key, HttpMethod.Put))
                    return@put
                }
            }.toMap()

            val set = sets.entries.find {
                it.key.size == options.size && it.key.all { property -> options.containsKey(property) }
            }


            check(set != null) {
                "Options set not found: ${options.keys.joinToString(",")}"
            }

            val sortedOptions = set.key.associateWith { property -> options[property] }

            val rollbackData = options.map { (key, _) ->
                key to OptionHandler.registered[key]!!.get().value
            }.toMap()

            try {
                val result = JsonObject(
                    sortedOptions.map { (key, newValue) ->
                        OptionHandler.registered[key]!!.let { handler ->
                            handler.setValue(newValue, shouldBeVerified = false)
                            key to handler.encodeToJsonElement()
                        }
                    }.toMap()
                )

                if (set.value != null) {
                    set.value!!.invoke()
                } else {
                    sortedOptions.forEach { (key, newValue) ->
                        object : Verification<Any?> {
                            override val previousValue: Any? = rollbackData[key]
                        }.apply {
                            OptionHandler.registered[key]!!.verification.invoke(this, newValue)
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