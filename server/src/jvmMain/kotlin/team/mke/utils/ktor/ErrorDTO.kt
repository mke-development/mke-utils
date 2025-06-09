package team.mke.utils.ktor

import io.github.smiley4.schemakenerator.core.annotations.Description
import io.ktor.http.HttpMethod
import kotlinx.serialization.Contextual
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import ru.raysmith.utils.nowZoned
import java.time.ZonedDateTime

/**
 * Объект ошибки API
 *
 * @property message *Human-readable* сообщение об ошибке. Можно отобразить пользователю.
 * @property description Дополнительное описание ошибки. Предназначено для разработчиков.
 * @property path Путь, по которому произошла ошибка
 * @property method HTTP-метод, использованный в запросе
 * @property timestamp Время возникновения ошибки
 * */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@Description("Объект ошибки")
data class ErrorDTO(

    @Description("*Human-readable* сообщение об ошибке. Можно отобразить пользователю.")
    @EncodeDefault
    val message: String = "Ошибка",

    @Description("Дополнительное описание ошибки. Предназначено для разработчиков.")
    @EncodeDefault
    val description: String? = null,

    @Description("Путь, по которому произошла ошибка")
    val path: String,

    @Description("HTTP-метод, использованный в запросе")
    @Serializable(HttpMethodSerializer::class)
    val method: HttpMethod,

    @Description("Время возникновения ошибки")
    @EncodeDefault
    @Contextual
    val timestamp: ZonedDateTime = nowZoned(),
)

