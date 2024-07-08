package team.mke.utils.logging

import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class LoggableCoroutineName(
    val name: String,
    val threadName: String? = null,
    val coroutineNameSeparator: String = "@"
) : ThreadContextElement<String>, AbstractCoroutineContextElement(
    LoggableCoroutineName
) {
    companion object Key : CoroutineContext.Key<LoggableCoroutineName>
    override fun toString(): String = "CoroutineName($name)"
    override fun updateThreadContext(context: CoroutineContext): String {
        val coroutineName = context[LoggableCoroutineName]?.name ?: "default-coroutine"
        val currentThread = Thread.currentThread()

        val oldName = currentThread.name
        var lastIndex = oldName.lastIndexOf(coroutineNameSeparator)
        if (lastIndex < 0) lastIndex = oldName.length
        val threadName = threadName ?: oldName.substring(0, lastIndex)

        currentThread.name = buildString(lastIndex + coroutineName.length + 5) {
            append(threadName)
            append(coroutineNameSeparator)
            append(coroutineName)
        }
        return oldName
    }
    override fun restoreThreadContext(context: CoroutineContext, oldState: String) {
        Thread.currentThread().name = oldState
    }
}