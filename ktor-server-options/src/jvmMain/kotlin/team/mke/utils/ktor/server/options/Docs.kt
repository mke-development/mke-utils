package team.mke.utils.ktor.server.options

import io.github.smiley4.ktoropenapi.config.RequestConfig
import io.github.smiley4.ktoropenapi.config.ResponseConfig
import io.github.smiley4.ktoropenapi.config.ResponsesConfig
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.config.SimpleBodyConfig
import io.ktor.http.HttpMethod
import io.swagger.v3.oas.models.media.Schema
import team.mke.utils.ktor.openapi.Method
import team.mke.utils.ktor.openapi.OpenApiRouteBlock
import team.mke.utils.ktor.openapi.ok
import team.mke.utils.model.OptionValue
import kotlin.reflect.typeOf

abstract class OptionMethodImpl : Method {
    var generator: Pair<OpenApiRouteBlock, OpenApiRouteBlock>? = null

    @get:JvmName("t")
    @set:JvmName("t")
    var test: OpenApiRouteBlock? = null

    var tags = setOf(OptionTag)
    var setupGet: RouteConfig.() -> Unit = {}
    var setupPut: RouteConfig.() -> Unit = {}
    var setupTest: RouteConfig.() -> Unit = {}

//    inline fun <reified T : Any?> getOptionValueSchema(schema: Schema<T>?): Schema<OptionValue<T>> {
//        if (schema != null) {
//            return Schema<OptionValue<T>>().apply {
//                title = "OptionValue<${schema.title}>"
//                description = "Представляет значение опции"
//                types = setOf("object")
//                properties = mapOf("value" to schema)
//            }
//        } else {
//            return Schema<OptionValue<T>>().apply {
//                name = "OptionValue"
//                title = "OptionValue"
//                description = "Представляет значение опции"
//                types = setOf("object")
//                properties = mapOf("value" to Schema<T>().apply {
//                    description = "Значение опции"
//                })
//            }
//        }
//    }

    inline fun <reified T : Any?> ResponseConfig.bodyFromSchemaOrType(
        schema: Schema<T>?, noinline block: SimpleBodyConfig.() -> Unit = {}
    ) {
        if (schema != null) {
            val optionalValueSchema = Schema<OptionValue<T>>().apply {
                title = schema.title?.let { "OptionValue<$it>" } ?: "OptionValue"
                description = "Представляет значение опции"
                types = setOf("object")
                properties = mapOf("value" to schema)
            }
            body(optionalValueSchema, block)
        } else {
            body<OptionValue<T>>(block)
        }
    }

    inline fun <reified T : Any?> RequestConfig.bodyFromSchemaOrType(
        schema: Schema<T>?, noinline block: SimpleBodyConfig.() -> Unit = {}
    ) {
        if (schema != null) {
            body(schema, block)
        } else {
            body<T>(block)
        }
    }

//    inline fun <reified T : Any?> getValueSchema(schema: Schema<T>?): Schema<T> {
//        return schema ?: Schema<T>().apply {
//            description = "Значение опции"
//        }
//    }

    @JvmName("generateF")
    inline fun <reified T : Any?> generate(
        name: String,
        responseExample: Pair<String, T>,
        requestExample: Pair<String, T>,
        summary: String? = null,
        crossinline setupGet: RouteConfig.() -> Unit = { this@OptionMethodImpl.setupGet(this) },
        crossinline setupPut: RouteConfig.() -> Unit = { this@OptionMethodImpl.setupPut(this) },
        tags: Set<String> = this.tags,
        schema: Schema<T>? = null,
        crossinline response: ResponsesConfig.(method: HttpMethod) -> Unit = {}
    ) {
        val Get: OpenApiRouteBlock = {
            description = "Возвращает $name"
            this.summary = summary
            this.tags = tags

            response {
                ok {
                    bodyFromSchemaOrType(schema) {
                        example(responseExample.first) {
                            value = OptionValue(responseExample.second)
                        }
                    }
                }
                response(HttpMethod.Get)
            }

            setupGet()
        }

        val Put: OpenApiRouteBlock = {
            description = "Устанавливает $name"
            this.summary = summary
            this.tags = tags

            request {
                bodyFromSchemaOrType(schema) {
                    required = true
                    example(requestExample.first) {
                        value = requestExample.second
                    }
                }
            }

            response {
                ok()
                response(HttpMethod.Put)
            }

            setupPut()
        }

        generator = Get to Put
    }

    @JvmName("testF")
    inline fun <reified T : Any?> test(
        description: String,
        requestExample: Pair<String, T>,
        crossinline setup: RouteConfig.() -> Unit = { this@OptionMethodImpl.setupTest(this) },
        tags: Set<String> = this.tags,
        schema: Schema<T>? = null,
        crossinline response: ResponsesConfig.(method: HttpMethod) -> Unit = {}
    ) {
        test = {
            this.description = description
            this.tags = tags

            request {
                bodyFromSchemaOrType(schema) {
                    required = true
                    example(requestExample.first) {
                        value = requestExample.second
                    }
                }
            }

            response {
                ok()
                response(HttpMethod.Post)
            }

            setup()
        }
    }

    override val Get get() = generator!!.first
    override val Put get() = generator!!.second

    override val Test get() = test
}
