package team.mke.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/** @sample team.mke.utils.test.serialization.BigDecimalSerializerTests */
object BigDecimalSerializer: KSerializer<BigDecimal> {
    val mathContext = MathContext(8, RoundingMode.HALF_UP)
    override fun deserialize(decoder: Decoder): BigDecimal {
        return decoder.decodeString().toBigDecimal()
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeDouble(
            value.round(mathContext).toPlainString().dropLastWhile { it == '0' }
                .ifEmpty { "0" }
                .let { if (it.lastOrNull() == '.') "${it}0" else it }
                .toDouble()
        )
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.DOUBLE)
}