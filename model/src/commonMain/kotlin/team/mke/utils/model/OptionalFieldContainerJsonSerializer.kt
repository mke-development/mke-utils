package team.mke.utils.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

abstract class OptionalFieldContainerJsonSerializer<T : Any>(serializer: KSerializer<T>) : JsonTransformingSerializer<T>(serializer) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        return when (element) {
            is JsonObject -> JsonObject(element.filterNot {
                (_, value) -> value is JsonPrimitive && value.content == OptionalFieldSerializer.notPresentKey
            })
            else -> element
        }
    }
}