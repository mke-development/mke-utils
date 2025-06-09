package team.mke.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.slf4j.Logger
import team.mke.utils.crashinterceptor.CrashInterceptor
import kotlin.reflect.KClass

/**
 * Сериализер для `enum`. использует [fallback] если не удалось десериализовать и вызывает [crashInterceptor],
 * если [reportError]=true.
 *
 * @sample team.mke.utils.serialization.test.EnumFallbackSerializerTests.Companion
 * */
open class EnumFallbackSerializer<E>(
    private val crashInterceptor: CrashInterceptor<*>, private val logger: Logger,
    private val kClass: KClass<E>, private val fallback: E, private val reportError: Boolean = true
) : KSerializer<E> where E : Enum<E> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("EnumSerialNameSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: E) {
        encoder.encodeString(value.getSerialName())
    }

    private fun reportError(value: String?, fallback: E) {
        val message = buildString {
            append("Не удалось десериализовать ").append(kClass.simpleName).append(" ")
            append("из полученного значения '").append(value).append("', ")
            append("использован fallback: ").append(fallback)
        }

        crashInterceptor.message(message, logger)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): E =
        if (decoder.decodeNotNullMark()) {
            decoder.decodeString().let { value ->
                kClass.java.enumConstants.firstOrNull { it.getSerialName() == value } ?: run {
                    if (reportError) {
                        reportError(value, fallback)
                    }
                    fallback
                }
            }
        } else {
            if (reportError) {
                reportError(null, fallback)
            }
            fallback
        }
}

