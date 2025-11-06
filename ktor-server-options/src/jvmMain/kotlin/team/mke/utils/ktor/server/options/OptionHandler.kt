package team.mke.utils.ktor.server.options

import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import team.mke.utils.model.OptionValue
import kotlin.reflect.KMutableProperty

@Suppress("UNCHECKED_CAST")
data class OptionHandler<T : Any?>(
    val property: KMutableProperty<T>,
    val serializer: KSerializer<T & Any>,
    val mutex: Mutex?,
    val json: Json,
    val get: suspend () -> OptionValue<T>,
    val edit: suspend (newValue: T, shouldBeVerified: Boolean) -> Unit,
    val encodeToJsonElement: suspend () -> JsonElement,
    val verification: Verification<T>.(newValue: T) -> Unit
) {
    companion object {
        internal val registered = mutableMapOf<String, OptionHandler<Any?>>()
    }

    fun decodeFromJsonElement(element: JsonElement) = json.decodeFromJsonElement(serializer, element)
    suspend fun setValue(newValue: Any?, shouldBeVerified: Boolean) {
        edit(newValue as T, shouldBeVerified)
    }

    fun register(path: String = property.name) {
        registered[path] = this as OptionHandler<Any?>
    }
}