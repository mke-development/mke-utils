package team.mke.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** @sample team.mke.utils.serialization.test.ZonedDateTimeSerializerTests */
object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        return ZonedDateTime.parse(decoder.decodeString(), formatter)
    }

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(formatter.format(value))
    }
}