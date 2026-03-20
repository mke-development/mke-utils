package team.mke.utils.ktor.server.pagination

import kotlinx.serialization.Serializable

@Serializable
data class PaginationData<T : Comparable<T>>(
    // schema described in SchemaConfig.applyPaginationDataSchema() TODO add docs

    val count: Int = -1,
    val lastEntity: T? = null,
)