package team.mke.utils.ktor.ext

import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException

fun notFound(message: String): Nothing = throw NotFoundException(message)
fun badRequest(message: String, cause: Throwable? = null): Nothing = throw BadRequestException(message, cause)
