@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")

package team.mke.utils.ktor.server.ext

import io.ktor.server.routing.*
import io.ktor.utils.io.*
import team.mke.utils.env.Environment

@KtorDsl
fun Route.devOnly(
    block: () -> Unit
) {
    if (Environment.isProd()) return
    return block()
}
