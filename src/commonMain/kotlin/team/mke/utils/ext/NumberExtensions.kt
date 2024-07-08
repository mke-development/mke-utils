package team.mke.utils.ext

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Форматирует количество байтов в читаемую строку округляя значение
 *
 * ```
 * 123.formatSize() // 123 б
 * 2060.formatSize() // 2 кб
 * ```
 *
 *
 * */
val Number.bytes: String get() {
    with(toLong()) {
        if (this <= 0) {
            return "0 б"
        }

        val units = arrayOf("б", "кб", "мб", "гб", "тб")
        val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()

        return "${this / 1024.0.pow(digitGroups.toDouble()).roundToInt()} ${units[digitGroups]}"
    }
}