package team.mke.utils.ktor.options

interface Verification <T> {
    val previousValue: T
}

fun <T> verification(previousValue: T, block: Verification<T>.() -> Unit) = object : Verification<T> {
    override val previousValue: T = previousValue
}.apply(block)