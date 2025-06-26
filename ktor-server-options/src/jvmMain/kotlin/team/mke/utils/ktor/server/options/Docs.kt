package team.mke.utils.ktor.server.options

import io.github.smiley4.ktoropenapi.config.ResponsesConfig
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.ktor.http.HttpMethod
import team.mke.utils.ktor.OptionValue
import team.mke.utils.ktor.openapi.Method
import team.mke.utils.ktor.openapi.OpenApiRouteBlock
import team.mke.utils.ktor.openapi.ok

abstract class OptionMethodImpl : Method {
    var generator: Pair<OpenApiRouteBlock, OpenApiRouteBlock>? = null

    @get:JvmName("t")
    @set:JvmName("t")
    var test: OpenApiRouteBlock? = null

    var tags = setOf(OptionTag)
    var setupGet: RouteConfig.() -> Unit = {}
    var setupPut: RouteConfig.() -> Unit = {}
    var setupTest: RouteConfig.() -> Unit = {}

    @JvmName("generateF")
    inline fun <reified T : Any> generate(
        name: String,
        responseExample: Pair<String, T>,
        requestExample: Pair<String, T>,
        summary: String? = null,
        crossinline setupGet: RouteConfig.() -> Unit = { this@OptionMethodImpl.setupGet(this) },
        crossinline setupPut: RouteConfig.() -> Unit = { this@OptionMethodImpl.setupPut(this) },
        tags: Set<String> = this.tags,
        crossinline response: ResponsesConfig.(method: HttpMethod) -> Unit = {}
    ) {
        val Get: OpenApiRouteBlock = {
            description = "Возвращает $name"
            this.summary = summary
            this.tags = tags

            response {
                ok {
                    body<OptionValue<T>> {
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
                body<T> {
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
    inline fun <reified T : Any> test(
        description: String,
        requestExample: Pair<String, T>,
        crossinline setup: RouteConfig.() -> Unit = { this@OptionMethodImpl.setupTest(this) },
        tags: Set<String> = this.tags,
        crossinline response: ResponsesConfig.(method: HttpMethod) -> Unit = {}
    ) {
        test = {
            this.description = description
            this.tags = tags

            request {
                body<T> {
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