package team.mke.utils.bg

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object Background {
    private object Lock
    private var isClosed = false

    val logger: Logger by lazy { LoggerFactory.getLogger("bg") }
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val processes: MutableMap<String, BackgroundProcess> = Collections.synchronizedMap(mutableMapOf())

    fun hasAnyProcess() = processes.isNotEmpty()
    fun processCount() = processes.size
    fun hasProcess(id: String) = findProcess(id) != null
    fun findProcess(id: String) = processes[id]

    private fun <T> sync(block: () -> T) {
        if (isClosed) return

        return synchronized(Lock) {
            block()
        }
    }

    fun registered(process: BackgroundProcess, throwOnRegistered: Boolean = true) = sync {
        if (hasProcess(process.id)) {
            if (throwOnRegistered) {
                error("Background process with id '${process.id}' already registered")
            } else {
                processes.remove(process.id)
            }
        }

        processes[process.id] = process
    }

    fun removeProcess(id: String) = sync {
        processes[id]?.let {
            processes.remove(id)
            true
        } ?: false
    }

    fun cancelAll() {
        isClosed = true
        scope.cancel()
        processes.forEach { (_, process) ->
            process.onCancel()
        }
        processes.clear()
    }

    fun restart(id: String) = sync {
        val process = findProcess(id) ?: return@sync
        process.cancel()
        process.onCancel()
        process.start(throwOnRegistered = false)
    }
}