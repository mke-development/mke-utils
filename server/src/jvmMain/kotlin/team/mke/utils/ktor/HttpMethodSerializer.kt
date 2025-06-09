package team.mke.utils.ktor

import io.ktor.http.HttpMethod
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object HttpMethodSerializer : KSerializer<HttpMethod> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HttpMethod", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: HttpMethod) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): HttpMethod {
        return HttpMethod.Companion.parse(decoder.decodeString())
    }
}