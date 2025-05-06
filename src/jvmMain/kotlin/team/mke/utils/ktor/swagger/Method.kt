package team.mke.utils.ktor.swagger

interface Method {
    val Get: OpenApiRouteBlock
    val Put: OpenApiRouteBlock
    val Test: OpenApiRouteBlock?
}