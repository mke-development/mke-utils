package team.mke.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
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

/** @sample team.mke.utils.serialization.test.BigDecimalSerializerTests */
object BigDecimalSerializer: KSerializer<BigDecimal> {
    const val scale = 8
    val roundingMode = RoundingMode.HALF_UP

    override fun deserialize(decoder: Decoder): BigDecimal {
        return when(decoder) {
            is JsonDecoder -> decoder.decodeJsonElement().jsonPrimitive.content.toBigDecimal()
            else -> decoder.decodeString().toBigDecimal()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        val rounded = value.setScale(scale, roundingMode).toPlainString().dropLastWhile { it == '0' }
            .ifEmpty { "0.0" }
            .let { if (it.lastOrNull() == '.') "${it}0" else it }

        when(encoder) {
            is JsonEncoder -> encoder.encodeJsonElement(JsonUnquotedLiteral(rounded))
            else -> encoder.encodeString(rounded)
        }
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.DOUBLE)
}