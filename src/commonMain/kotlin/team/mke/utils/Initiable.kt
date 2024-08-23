package team.mke.utils

interface Initiable {
    fun init() = run { }
}

abstract class BaseInitiable : Initiable, AutoCloseable {
    var isInit = false
        private set

    override fun init() {
        isInit = true
    }

    override fun close() {
        isInit = false
    }
}

abstract class InitiableWithArgs<T> : BaseInitiable() {
    open fun init(data: T) {
        super.init()
    }
}