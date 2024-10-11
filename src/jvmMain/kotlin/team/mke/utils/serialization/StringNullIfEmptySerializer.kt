package team.mke.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object StringNullIfEmptySerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringNullIfEmpty", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): String? {
        return if (decoder.decodeNotNullMark()) {
            decoder.decodeString().ifEmpty { null }
        } else {
            decoder.decodeNull()
        }
    }

    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value ?: "")
    }
}