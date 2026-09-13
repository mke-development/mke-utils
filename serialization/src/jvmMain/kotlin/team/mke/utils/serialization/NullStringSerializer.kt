package team.mke.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * *KSerializer*, который десериализует `null` из строки "null" или из реального `null` значения.
 *
 * @param defaultSerializer обычный *serializer* для *not-null* значения типа [T]
 * @param convert функция конвертации из полученного значения в тип [T]
 * @param ignoreCase игнорировать регистр при сравнении строки с "null". По умолчанию `false`.
 */
@OptIn(ExperimentalSerializationApi::class)
abstract class NullStringSerializer<T>(
    val defaultSerializer: SerializationStrategy<T & Any>,
    val convert: (String) -> T,
    val ignoreCase: Boolean = false
) : KSerializer<T?> {

    constructor(s: KSerializer<T & Any>, h: (String) -> T) : this(s, h, false)

    override val descriptor: SerialDescriptor = defaultSerializer.descriptor

    override fun deserialize(decoder: Decoder): T? {
        if (decoder.decodeNotNullMark()) {
            val str = decoder.decodeString()
            if (str.equals("null", ignoreCase)) {
                return null
            }

            return convert(str)
        } else {
            return decoder.decodeNull()
        }
    }

    override fun serialize(encoder: Encoder, value: T?) {
        encoder.encodeNullableSerializableValue(defaultSerializer, value)
    }
}
