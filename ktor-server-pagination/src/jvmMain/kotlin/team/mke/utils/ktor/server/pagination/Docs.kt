package team.mke.utils.ktor.server.pagination

import io.github.smiley4.ktoropenapi.config.RequestConfig

inline fun <reified T : Comparable<T>> RequestConfig.pagination(
    filedName: String, entityFormat: String? = null, min: Int = 1, max: Int = 100, default: Int = 20,
    additionalDescription: String? = null, sortableFields: List<String>? = null
) {
    queryParameter("p", PaginationDataSchema<T>(filedName, entityFormat, min, max, default)) {
        required = false
        description = "Пагинация по полю $filedName"
        if (sortableFields != null) {
            description += "</br></br>Доступные поля для сортировки:\n${sortableFields.joinToString("\n") { "- `$it`" }}"
        }
        if (additionalDescription != null) {
            description += "</br></br>$additionalDescription"
        }
    }
}
