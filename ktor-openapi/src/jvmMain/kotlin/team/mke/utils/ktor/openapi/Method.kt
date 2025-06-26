package team.mke.utils.ktor.openapi

@Suppress("PropertyName")
interface Method {
    val Get: OpenApiRouteBlock
    val Put: OpenApiRouteBlock
    val Test: OpenApiRouteBlock?
}