package team.mke.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import team.mke.utils.ext.PhoneFormat
import team.mke.utils.ext.defaultExportPhonesFormats
import team.mke.utils.ext.exportPhones

@OptIn(ExperimentalSerializationApi::class)
open class PhoneSerializer(vararg formats: PhoneFormat = defaultExportPhonesFormats) : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Phone", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeNotNullMark()
            encoder.encodeString(value)
        }
    }

    override fun deserialize(decoder: Decoder): String? {
        return decoder.decodeString().exportPhones().firstOrNull()
    }
}

object DefaultPhoneSerializer : PhoneSerializer()