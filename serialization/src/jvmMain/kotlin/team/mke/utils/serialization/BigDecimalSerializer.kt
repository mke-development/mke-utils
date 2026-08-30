package team.mke.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal
import java.math.RoundingMode

// TODO [test] is that possible to remove kotlinx.serialization.json dependency?

open class BigDecimalSerializer(val scale: Int? = null, val roundingMode: RoundingMode = RoundingMode.HALF_UP): KSerializer<BigDecimal> {

    override fun deserialize(decoder: Decoder): BigDecimal {
        val value = when(decoder) {
            is JsonDecoder -> decoder.decodeJsonElement().jsonPrimitive.content.toBigDecimal()
            else -> decoder.decodeString().toBigDecimal()
        }

        return if (scale != null) {
            value.setScale(scale, roundingMode)
        } else {
            value
        }
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        val fixedScaleValue = if (value.scale() == 0) {
            value.setScale(1)
        } else {
            value
        }

        when(encoder) {
            is JsonEncoder -> encoder.encodeJsonElement(JsonUnquotedLiteral(fixedScaleValue.toPlainString()))
            else -> encoder.encodeString(fixedScaleValue.toPlainString())
        }
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.DOUBLE)

    companion object : BigDecimalSerializer()
}
