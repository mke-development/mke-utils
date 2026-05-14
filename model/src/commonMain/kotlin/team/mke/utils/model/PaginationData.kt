package team.mke.utils.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PaginationData")
data class PaginationData<T : Comparable<T>>(
    val count: Int = -1,
    val lastEntity: T? = null,
    val lastSortedValue: String? = null,
    val sortBy: String? = null,
    val sort: Sort? = null
) {
    init {
        if (lastSortedValue != null) {
            require(lastEntity != null) {
                "Both lastEntity and lastSortedValue must be provided together"
            }
        }
    }
}

@Serializable
enum class Sort {
    ASC,
    DESC
}
