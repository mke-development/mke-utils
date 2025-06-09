package team.mke.utils.ktor.server.options

import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.dsl.routing.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveNullable
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.route
import io.ktor.util.reflect.reifiedType
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializerOrNull
import ru.raysmith.exposedoption.Option
import ru.raysmith.utils.letIf
import ru.raysmith.utils.wrap
import team.mke.utils.ktor.OptionValue
import team.mke.utils.ktor.swagger.Method
import team.mke.utils.ktor.swagger.OpenApiRouteBlock
import kotlin.jvm.internal.MutablePropertyReference0
import kotlin.reflect.KMutableProperty
import kotlin.reflect.jvm.isAccessible

class OptionsRoutingContext {
    companion object {
        internal val sets = mutableMapOf<Set<String>, (() -> Unit)?>()
    }

    context(OptionsRoutingContext)
    fun registerSet(vararg properties: KMutableProperty<*>, verification: (() -> Unit)? = null) {
        sets[properties.map { it.name }.toSet()] = verification
    }

    context(OptionsRoutingContext)
    fun registerSet(vararg properties: String, verification: (() -> Unit)? = null) {
        sets[properties.toSet()] = verification
    }

    context(Route, OptionsPluginConfiguration)
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any?> setup(
        property: KMutableProperty<T>,
        docs: Method,
        mutex: Mutex? = null,
        path: String = property.name,
        returnOnLocked: Boolean = true,
        serializer: KSerializer<T & Any> = (serializerOrNull(typeInfo<T>().reifiedType)
            ?: ContextualSerializer(typeInfo<T>().type)) as KSerializer<T & Any>,
        noinline test: (suspend RoutingContext.(T?) -> Unit)? = null,
        noinline verification: Verification<T>.(newValue: T) -> Unit = {}
    ) = setup(property, docs.Get, docs.Put, docs.Test, mutex, path, returnOnLocked, serializer, test, verification)

    context(Route, OptionsPluginConfiguration)
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any?> setup(
        property: KMutableProperty<T>,
        noinline docsGet: OpenApiRouteBlock,
        noinline docsPut: OpenApiRouteBlock,
        noinline docsTest: OpenApiRouteBlock?,
        mutex: Mutex? = null,
        path: String = property.name,
        returnOnLocked: Boolean = true,
        serializer: KSerializer<T & Any> = (serializerOrNull(typeInfo<T>().reifiedType)
            ?: ContextualSerializer(typeInfo<T>().type)) as KSerializer<T & Any>,
        noinline test: (suspend RoutingContext.(T?) -> Unit)? = null,
        noinline verification: Verification<T>.(newValue: T) -> Unit = {}
    ) {

        property.isAccessible = true
        val delegate = (property as MutablePropertyReference0).getDelegate()!! as Option<T>

        route(path) {
            val handler = OptionHandler(
                property = property,
                serializer = serializer,
                mutex = mutex,
                get = {
                    OptionValue(delegate.value)
                },
                edit = { newValue, shouldBeVerified ->
                    val previousValue = delegate.value
                    try {
                        delegate.set(newValue)
                        if (shouldBeVerified) {
                            verification(previousValue) {
                                verification(newValue)
                            }
                        }
                    } catch (e: Exception) {
                        delegate.set(previousValue)
                        throw e
                    }
                },
                encodeToJsonElement = {
                    try {
                        json.encodeToJsonElement(OptionValue.serializer(serializer), OptionValue(delegate.value))
                    } catch (e: SerializationException) {
                        throw IllegalStateException("Option '$path' can't be serialized", e)
                    }
                },
                verification = verification,
                json = json
            )

            handler.register(path)

            get(docsGet) {
                call.respond(handler.get())
            }

            put(docsPut) {
                if (returnOnLocked && handler.mutex != null && handler.mutex.isLocked) {
                    call.respond(HttpStatusCode.Locked)
                    return@put
                }

                val newValue = suspend {
                    call.receiveText().let { text ->
                        handler.json.decodeFromString<T>(
                            handler.serializer,
                            text.letIf({ !it.startsWith("[") && !it.startsWith("{") }) { text ->
                                text
                                    .letIf(text.startsWith("\"")) { it.drop(1) }
                                    .letIf(text.endsWith("\"")) { it.dropLast(1) }
                                    .wrap('"')
                            }
                        )
                    }
                }

                if (handler.mutex != null) {
                    handler.mutex.withLock {
                        handler.edit(newValue(), true)
                        call.respond(HttpStatusCode.OK)
                    }
                } else {
                    handler.edit(newValue(), true)
                    call.respond(HttpStatusCode.OK)
                }
            }

            if (test != null) {
                post("/test", docsTest ?: {}) {
                    val body = call.receiveNullable<T>()
                    test(body)
                }
            }
        }
    }
}