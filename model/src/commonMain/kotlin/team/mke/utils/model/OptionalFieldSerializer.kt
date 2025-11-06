package team.mke.utils.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

@OptIn(InternalSerializationApi::class)
open class OptionalFieldSerializer<T>(
    private val valueSerializer: KSerializer<T>
) : KSerializer<OptionalField<T>> {

    companion object {
        val notPresentKey = "npk_${UUID.randomUUID()}"
    }

    override val descriptor: SerialDescriptor = buildSerialDescriptor("OptionalField", SerialKind.CONTEXTUAL) {
        element("value", valueSerializer.descriptor)
    }

    override fun deserialize(decoder: Decoder): OptionalField<T> =
        OptionalField.Present(decoder.decodeSerializableValue(valueSerializer))

    override fun serialize(encoder: Encoder, value: OptionalField<T>) {
        when (value) {
            is OptionalField.Present -> valueSerializer.serialize(encoder, value.value)
            OptionalField.NotPresent -> encoder.encodeString(notPresentKey)
        }
    }
}