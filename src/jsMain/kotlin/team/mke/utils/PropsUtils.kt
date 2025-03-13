package team.mke.utils

import js.objects.Object
import js.objects.jso
import react.Props

/**
 * ```typescript
 * const { propOne, propTwo, ...other } = props;
 * ```
 *
 * @return `other`
 * */
fun Props.other(vararg prop: String): Props {
    val res = jso<Props> {}
    
    Object.keys(this)
        .filter { it !in prop }
        .forEach {
            val e = this[it]
            if (e != null) {
                res[it] = e
            }
        }
    
    val extraProps = prop.filter { it.contains(".") }
    extraProps.forEach {
        val paths = it.split(".")
        if (Object.hasOwn(this, paths.first())) {
            res[paths.first()] = (this[paths.first()].unsafeCast<Props>()).other(paths.drop(1).joinToString("."))
        }
    }
    
    return res
}

operator fun Props.get(key: dynamic) = asDynamic()[key]
operator fun Props.set(key: dynamic, value: dynamic) {
    asDynamic()[key] = value
}