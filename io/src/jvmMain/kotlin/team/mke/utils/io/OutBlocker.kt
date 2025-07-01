package team.mke.utils.io

import java.util.UUID

class OutBlocker() : AutoCloseable {

    companion object {
        private val blockers = mutableListOf<OutBlocker>()
    }

    private val id = UUID.randomUUID()

    fun block() {
        System.setOut(disabledOutputStream)
    }

    fun release() = close()

    override fun close() {
        System.setOut(originalOut)
        blockers.remove(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OutBlocker) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}