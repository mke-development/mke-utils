package team.mke.utils.ktor.ext

import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Возвращает существующее значение атрибута [key] или вычисляет его с помощью [value],
 * сохраняет в [Attributes] и возвращает.
 *
 * Если [value] возвращает `null`, значение в атрибуты не сохраняется, и метод возвращает `null`.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T : Any> Attributes.findOrSet(key: AttributeKey<T>, value: () -> T?): T? {
    contract {
        callsInPlace(value, InvocationKind.AT_MOST_ONCE)
    }
    return if (contains(key)) {
        getOrNull(key)
    } else {
        value()?.also {
            put(key, it)
        }
    }
}
