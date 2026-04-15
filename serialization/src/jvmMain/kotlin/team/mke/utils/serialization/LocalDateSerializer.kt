package team.mke.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** @sample team.mke.utils.serialization.test.LocalDateSerializerTests */
open class LocalDateSerializer(val formatter: DateTimeFormatter) : KSerializer<LocalDate> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), formatter)
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(formatter.format(value))
    }

    object Factory {
        fun create(formatter: DateTimeFormatter) = LocalDateSerializer(formatter)
        fun create(format: String) = LocalDateSerializer(DateTimeFormatter.ofPattern(format))
    }

    companion object : LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE)
}
