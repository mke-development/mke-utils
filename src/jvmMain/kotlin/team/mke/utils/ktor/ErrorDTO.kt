package team.mke.utils.ktor

import kotlinx.serialization.Contextual
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import ru.raysmith.utils.nowZoned
import java.time.ZonedDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ErrorDTO(
    @EncodeDefault val message: String = "Ошибка",
    @EncodeDefault val description: String? = null,
    val path: String,
    val method: String,
    @Contextual val timestamp: ZonedDateTime = nowZoned(),
)