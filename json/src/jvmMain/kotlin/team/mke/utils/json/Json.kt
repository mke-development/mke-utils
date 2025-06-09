package team.mke.utils.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.modules.SerializersModule
import team.mke.utils.env.Environment
import team.mke.utils.serialization.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * Creates an instance of Json configured from the optionally given Json instance and adjusted with builderAction.
 *
 * Создает экземпляр [kotlinx.serialization.json.Json] используя настройки [from] -> [applyDefaultJson] -> [builderAction]
 * */
fun json(from: Json, builderAction: JsonBuilder.() -> Unit) = Json(from) {
    applyDefaultJson()
    builderAction()
}

/**
 * [kotlinx.serialization.json.Json] по умолчанию
 *
 * @see applyDefaultJson
 * */
val json = Json {
    applyDefaultJson()
}

/**
 * Устанавливает данные по умолчанию
 *
 * ```
 * isLenient = true
 * ignoreUnknownKeys = true
 * prettyPrint = Environment.isDev()
 * serializersModule = SerializersModule {
 *     contextual(LocalDate::class, LocalDateSerializer)
 *     contextual(ZonedDateTime::class, ZonedDateTimeSerializer)
 *     contextual(BigDecimal::class, BigDecimalSerializer)
 *     safe {
 *         Class.forName("kotlinx.datetime.LocalDate")
 *         contextual(kotlinx.datetime.LocalDate::class, KotlinxLocalDateSerializer)
 *     }
 * }
 * ```
 * */
private fun JsonBuilder.applyDefaultJson() {
    isLenient = true
    ignoreUnknownKeys = true
    prettyPrint = Environment.isDev()
    serializersModule = SerializersModule {
        contextual(LocalDate::class, LocalDateSerializer)
        contextual(LocalTime::class, LocalTimeSerializer)
        contextual(ZonedDateTime::class, ZonedDateTimeSerializer)
        contextual(BigDecimal::class, BigDecimalSerializer)
        try {
            Class.forName("kotlinx.datetime.LocalDate")
            contextual(kotlinx.datetime.LocalDate::class, KotlinxLocalDateSerializer)
        } catch (_: Exception) {
            // Ignore if kotlinx.datetime is not available
        }
    }
}