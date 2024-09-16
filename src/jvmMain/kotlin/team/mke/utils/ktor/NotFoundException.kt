package team.mke.utils.ktor

fun notFound(message: String): Nothing = throw NotFoundException(message)

class NotFoundException(message: String) : RuntimeException(message)