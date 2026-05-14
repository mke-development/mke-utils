package team.mke.utils.ktor

@Deprecated("use utils for EntityNotFoundException or respond 404")
fun notFound(message: String): Nothing = throw NotFoundException(message)

@Deprecated("use EntityNotFoundException or respond 404")
class NotFoundException(message: String) : RuntimeException(message)
