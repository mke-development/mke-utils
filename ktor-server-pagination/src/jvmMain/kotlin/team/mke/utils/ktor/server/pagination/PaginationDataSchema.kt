package team.mke.utils.ktor.server.pagination

import io.swagger.v3.oas.models.media.Schema
import ru.raysmith.utils.orNotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

inline fun <reified T> PaginationDataSchema(
    filedName: String? = null, entityFormat: String? = null, min: Int? = null, max: Int? = null, default: Int? = null
) = Schema<PaginationData<T>>().apply {
    types = setOf("object")
    properties = mapOf(
        "count" to Schema<Int>().apply {
            types = setOf("integer")
            format = "int32"
            description = buildString {
                append("Количество элементов на странице")
                if (min orNotNull max orNotNull default) {
                    append(". Диапазон и значение по умолчанию могут отличаться в зависимости от метода.")
                }
            }
            if (min != null) {
                minimum = BigDecimal(min)
            }
            if (max != null) {
                maximum = BigDecimal(max)
            }
            if (default != null) {
                setDefault(default)
            }
        },
        "lastEntity" to Schema<Any>().apply {
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

            if (filedName == null) {
                description = buildString {
                    append("Значение последнего элемента на странице. ")
                    append("Используется для получения следующей страницы, `null` для первой страницы. ")
                    append("Может быть любого типа в зависимости от модели (см. общую информацию о пагинации)")
                }
            } else {
                description = "Значение поля '$filedName' последнего элемента с предыдущей страницы"
            }
        },
        "lastSortedValue" to Schema<String>().apply {
            types = setOf("string")
            nullable = true
            description = buildString {
                append("Значение поля последнего элемента на странице, по которому происходит сортировка. ")
                append("Используется для получения следующей страницы при использовании `sortBy`, ")
                    .append("`null` для первой страницы.")
            }
        },
        "sortBy" to Schema<String>().apply {
            types = setOf("string")
            nullable = true
            description = "Поле, по которому происходит сортировка"
        },
        "sort" to Schema<String>().apply {
            types = setOf("string")
            enum = Sort.entries.map { it.name }
            description = "Направление сортировки"
        },
    )
}
