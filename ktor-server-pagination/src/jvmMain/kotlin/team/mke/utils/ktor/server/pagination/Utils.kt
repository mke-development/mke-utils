package team.mke.utils.ktor.server.pagination

import io.github.smiley4.ktoropenapi.config.SchemaConfig
import io.ktor.server.application.ApplicationCall
import io.swagger.v3.oas.models.media.Schema
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import ru.raysmith.utils.letIf
import team.mke.utils.locale.i18n
import team.mke.utils.ktor.ext.json.get
import team.mke.utils.locale.I18n
import java.util.Locale

fun SchemaConfig.applyPaginationDataSchema(
    countDescription: String = "Количество элементов на странице. Диапазон и значение по умолчанию могут отличаться в зависимости от метода.",
    lastEntityDescription: String = "Значение последнего элемента на странице. Используется для получения следующей страницы, `null` для первой страницы. Может быть любого типа в зависимости от модели (см. общую информацию о пагинации)",
    config: Schema<PaginationData<*>>.() -> Unit = {}
) {
    schema("PaginationData", Schema<PaginationData<*>>().apply {
        types = setOf("object")
        properties = mapOf(
            "count" to Schema<Int>().apply {
                types = setOf("integer")
                format = "int32"
                description = countDescription
            },
            "lastEntity" to Schema<Any>().apply {
                types = setOf("any")
                description = lastEntityDescription
            }
        )
        config()
    })
}

@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
inline fun <reified T : Comparable<T>> ApplicationCall.paginationData(
    min: Int = 1, max: Int = 100, default: Int = 20,
    locale: Locale = I18n.defaultLocale,
    parameterName: String = "p",
    json: Json = team.mke.utils.json.json,
    rangeError: () -> String = { i18n("error.pagination.count.range", locale, min, max) }
): PaginationData<T> {
    check(min <= max) { "Minimum value must be less than or equal to maximum value" }

    val data = run {
        parameters.get(
            parameterName, PaginationData.serializer(T::class.serializerOrNull() ?: ContextualSerializer(T::class)), json
        ) ?: PaginationData()
    }.letIf({ it.count == -1 }) { it.copy(count = default) }

    require(data.count in min..max, rangeError)

    return data
}
