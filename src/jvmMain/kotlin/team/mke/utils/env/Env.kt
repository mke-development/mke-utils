package team.mke.utils.env

import kotlin.reflect.KProperty

private val envs = mutableListOf<Env<*>>()

/**
 * Обертка со значением переменной окружения
 *
 * @see env
 * @see envRequired
 * */
class Env<T : Any?>(val key: String, private val required: Boolean = false, private val getter: (key: String) -> T) {
    private var init = false
    private var cache: T? = null

    companion object {
        fun refresh() {
            envs.forEach {
                it.refresh()
            }
        }
    }

    init {
        envs.add(this)
        if (required) {
            getValue(null, null) ?: throwMissingEnv(key)
        }
    }

    /** Возвращает значение */
    fun get() = getValue(null, null)

    operator fun getValue(thisRef: Any?, property: KProperty<*>?): T {
        if (!init) {
            cache = getter(key)
            init = true
        }
        @Suppress("UNCHECKED_CAST")
        return cache as T
    }

    /** Обновляет кэш из [getter] */
    fun refresh(): T {
        cache = getter(key)

        @Suppress("UNCHECKED_CAST")
        return cache as T
    }
}