package team.mke.utils.ktor.server.options

import io.github.smiley4.ktorswaggerui.dsl.routes.OpenApiResponses
import io.github.smiley4.ktorswaggerui.dsl.routes.OpenApiRoute
import io.ktor.http.HttpMethod
import team.mke.utils.ktor.OptionValue
import team.mke.utils.ktor.swagger.Method
import team.mke.utils.ktor.swagger.OpenApiRouteBlock
import team.mke.utils.ktor.swagger.ok

abstract class OptionMethodImpl : Method {
    var generator: Pair<OpenApiRouteBlock, OpenApiRouteBlock>? = null

    @get:JvmName("t")
    @set:JvmName("t")
    var test: OpenApiRouteBlock? = null

    var tags = setOf(OptionTag)
    var setupGet: OpenApiRoute.() -> Unit = {}
    var setupPut: OpenApiRoute.() -> Unit = {}
    var setupTest: OpenApiRoute.() -> Unit = {}

    @JvmName("generateF")
    inline fun <reified T : Any> generate(
        name: String,
        responseExample: Pair<String, T>,
        requestExample: Pair<String, T>,
        summary: String? = null,
        crossinline setupGet: OpenApiRoute.() -> Unit = { this@OptionMethodImpl.setupGet(this) },
        crossinline setupPut: OpenApiRoute.() -> Unit = { this@OptionMethodImpl.setupPut(this) },
        tags: Set<String> = this.tags,
        crossinline response: OpenApiResponses.(method: HttpMethod) -> Unit = {}
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
        crossinline setup: OpenApiRoute.() -> Unit = { this@OptionMethodImpl.setupTest(this) },
        tags: Set<String> = this.tags,
        crossinline response: OpenApiResponses.(method: HttpMethod) -> Unit = {}
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