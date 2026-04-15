package team.mke.utils.ktor.server.options

import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
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
import team.mke.utils.ktor.openapi.Method
import team.mke.utils.ktor.openapi.OpenApiRouteBlock
import team.mke.utils.model.OptionValue
import kotlin.jvm.internal.MutablePropertyReference0
import kotlin.reflect.KMutableProperty
import kotlin.reflect.jvm.isAccessible

class OptionsRoutingContext {
    companion object {
        internal val sets = mutableMapOf<Set<String>, (() -> Unit)?>()
    }

    context(_: OptionsRoutingContext)
    fun registerSet(vararg properties: KMutableProperty<*>, verification: (() -> Unit)? = null) {
        sets[properties.map { it.name }.toSet()] = verification
    }

    context(_: OptionsRoutingContext)
    fun registerSet(vararg properties: String, verification: (() -> Unit)? = null) {
        sets[properties.toSet()] = verification
    }

    context(_: Route, config: OptionsPluginConfiguration)
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any?> setup(
        property: KMutableProperty<T>,
        docs: Method,
        mutex: Mutex? = null,
        path: String = property.name,
        returnOnLocked: Boolean = true,
        serializer: KSerializer<T & Any> = (config.json.serializersModule.serializerOrNull(typeInfo<T>().reifiedType)
            ?: ContextualSerializer(typeInfo<T>().type)) as KSerializer<T & Any>,
        noinline test: (suspend RoutingContext.(T?) -> Unit)? = null,
        noinline verification: Verification<T>.(newValue: T) -> Unit = {}
    ) = setup(property, docs.Get, docs.Put, docs.Test, mutex, path, returnOnLocked, serializer, test, verification)

    context(route: Route, config: OptionsPluginConfiguration)
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
        serializer: KSerializer<T & Any> = (config.json.serializersModule.serializerOrNull(typeInfo<T>().reifiedType)
            ?: ContextualSerializer(typeInfo<T>().type)) as KSerializer<T & Any>,
        noinline test: (suspend RoutingContext.(T?) -> Unit)? = null,
        noinline verification: Verification<T>.(newValue: T) -> Unit = {}
    ) {

        property.isAccessible = true
        val delegate = (property as MutablePropertyReference0).getDelegate()!! as Option<T>

        route.route(path) {
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
                        config.json.encodeToJsonElement(OptionValue.serializer(serializer), OptionValue(delegate.value))
                    } catch (e: SerializationException) {
                        throw IllegalStateException("Option '$path' can't be serialized", e)
                    }
                },
                verification = verification,
                json = config.json
            )

            handler.register(path)

            get(docsGet) {
                call.respond(config.json.encodeToJsonElement(
                    serializer = OptionValue.serializer(serializer),
                    value = handler.get() as OptionValue<T & Any>
                ))
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
