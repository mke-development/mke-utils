package team.mke.utils.logging

import org.slf4j.Logger

typealias ErrorTag = Pair<String, Any?>

fun Logger.debug(e: Throwable) = debug(e.message, e)
fun Logger.error(e: Throwable) = error(e.message, e)

fun tags(vararg tags: ErrorTag) = arrayOf(*tags)
fun tags(e: Exception, vararg tags: ErrorTag): Array<ErrorTag> {
    return if (e is IllegalStateExceptionWithTags) {
        arrayOf(*tags, *e.tags)
    } else {
        arrayOf(*tags)
    }
}

operator fun Array<ErrorTag>.plus(e: Exception) = tags(e, *this)

fun error(message: String, vararg tags: ErrorTag): Nothing =
    throw IllegalStateExceptionWithTags(message, *tags)
