package team.mke.utils

interface Initiable {
    fun init() = run { }
}

abstract class BaseInitiable : Initiable {
    var isInit = false
        private set

    override fun init() {
        isInit = true
    }
}

abstract class InitiableWithArgs<T> : BaseInitiable() {
    open fun init(data: T) {
        super.init()
    }
}