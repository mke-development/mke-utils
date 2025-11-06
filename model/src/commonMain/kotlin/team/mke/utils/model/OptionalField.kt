package team.mke.utils.model

import kotlinx.serialization.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable(OptionalFieldSerializer::class)
sealed class OptionalField<out T> {
    data object NotPresent : OptionalField<Nothing>()
    data class Present<T>(val value: T) : OptionalField<T>()

    @OptIn(ExperimentalContracts::class)
    inline fun isPresentAnd(predicate: (T) -> Boolean, block: (T) -> Unit) {
        contract {
            returns(true) implies (this@OptionalField is Present)
        }
        if (this is Present && predicate(value)) {
            block(value)
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun isPresent(block: (T) -> Unit) {
        contract {
            returns(true) implies (this@OptionalField is Present)
        }
        if (this is Present) {
            block(value)
        }
    }

    @OptIn(ExperimentalContracts::class)
    fun isPresent(): Boolean {
        contract {
            returns(true) implies (this@OptionalField is Present<T>)
        }
        return this is Present
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <R> ifPresentOrNull(block: ((T) -> R)): R? {
        contract {
            returns(true) implies (this@OptionalField is Present<T>)
        }

        return if (this is Present) {
            block(value)
        } else null
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <R> ifPresentAndOrNull(predicate: (T) -> Boolean = { true }, block: (T) -> R): R? {
        contract {
            returns(true) implies (this@OptionalField is Present<T>)
        }

        return if (this is Present && predicate(value)) {
            block(value)
        } else null
    }

    inline fun or(default: () -> @UnsafeVariance T): T = when (this) {
        is Present -> value
        NotPresent -> default()
    }

    fun orNull() = when (this) {
        is Present -> value
        NotPresent -> null
    }

    inline fun orThrow(error: () -> Nothing = { error("Value is not present") }): T = when (this) {
        is Present -> value
        NotPresent -> error()
    }

    @OptIn(ExperimentalContracts::class)
    inline fun isPresentAndNotNull(predicate: (T) -> Boolean = { true }, block: (T) -> Unit) {
        contract {
            returns(true) implies (this@OptionalField is Present<T>)
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (this is Present && value != null && predicate(value)) {
            block(value)
        }
    }
}