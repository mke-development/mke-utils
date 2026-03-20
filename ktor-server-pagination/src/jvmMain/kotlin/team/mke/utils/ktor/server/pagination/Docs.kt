package team.mke.utils.ktor.server.pagination

import io.github.smiley4.ktoropenapi.config.RequestConfig
import io.swagger.v3.oas.models.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

inline fun <reified T : Comparable<T>> RequestConfig.pagination(
    filedName: String, entityFormat: String? = null, min: Int = 1, max: Int = 100, default: Int = 20,
) {
    queryParameter("p", Schema<PaginationData<T>>().apply {
        title = "PaginationData"
        types = setOf("object")
        properties = mapOf(
            "count" to Schema<Int>().apply {
                types = setOf("integer")
                format = "int32"
                description = "Количество элементов на странице"
                maximum = BigDecimal(min)
                minimum = BigDecimal(max)
                setDefault(default)
            },
            "lastEntity" to Schema<T>().apply {
                types = setOf(when(T::class) {
                    String::class -> "string"
                    Int::class, Short::class, Byte::class, Long::class -> "integer"
                    Float::class, Double::class, BigDecimal::class -> "number"
                    Boolean::class -> "boolean"
                    LocalDate::class, LocalDateTime::class, ZonedDateTime::class -> "string"
                    else -> "any"
                })
                format = entityFormat ?: when(T::class) {
                    Int::class, Short::class, Byte::class -> "int32"
                    Long::class -> "int64"
                    LocalDate::class -> "dd.MM.yyyy"
                    LocalDateTime::class -> "dd.MM.yyyyTHH:mm:ss"
                    ZonedDateTime::class -> "dd.MM.yyyyTHH:mm:ss+hh:mm"
                    else -> null
                }
                description = "Значение поля '$filedName' последнего элемента с предыдущей страницы"
            }
        )
    }) {
        required = false
        description = "Пагинация по полю $filedName"
    }
}