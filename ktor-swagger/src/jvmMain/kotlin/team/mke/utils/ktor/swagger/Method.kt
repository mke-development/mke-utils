package team.mke.utils.ktor.swagger

@Suppress("PropertyName")
interface Method {
    val Get: OpenApiRouteBlock
    val Put: OpenApiRouteBlock
    val Test: OpenApiRouteBlock?
}