package team.mke.utils.model

import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Name
import io.ktor.http.HttpMethod
import io.swagger.v3.oas.annotations.media.Schema
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
@Name("ErrorDTO")
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
    @field:Schema(type = "string", format = "GET|POST|PUT|PATCH|DELETE|OPTIONS|HEAD|TRACE|CONNECT")
    val method: HttpMethod,

    @Description("Время возникновения ошибки")
    @EncodeDefault
    @Contextual
    val timestamp: ZonedDateTime = nowZoned(),
)

