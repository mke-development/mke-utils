package team.mke.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** @sample team.mke.utils.serialization.test.BooleanAsIntSerializerTests */
object BooleanAsIntSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BooleanAsInt", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeInt(if (value)1 else 0)
    }

    override fun deserialize(decoder: Decoder): Boolean = when(val value = decoder.decodeInt()) {
        0 -> false
        1 -> true
        else -> error("Only 0 or 1 values allowed for BooleanAsInt but received: $value")
    }
}