package team.mke.utils.ktor.server.ext

import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.host
import io.ktor.server.request.port

fun ApplicationCall.serverBaseUrl(): String {
    return buildString {
        append(request.origin.scheme)
        append("://")
        append(request.host())
        append(request.port().let {
            if (it != 80 && it != 443) ":$it" else ""
        })
    }
}
