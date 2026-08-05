package team.mke.utils

import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Форматирует количество байтов в читаемую строку округляя значение
 *
 * ```
 * 123.bytes // 123 б
 * 2060.bytes // 2 кб
 * ```
 *
 * @param locale Язык для форматирования. Поддерживаются только `ru` и `en`.
 * */
fun Number.bytes(locale: Locale): String = with(toLong()) {
    val units = when(locale.language) {
        "en" -> arrayOf("b", "kb", "mb", "gb", "tb")
        "ru" -> arrayOf("б", "кб", "мб", "гб", "тб")
        else -> throw IllegalArgumentException("Unsupported locale: $locale")
    }

    if (this <= 0) {
        return "0 ${units.first()}"
    }

    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
    return "${this / 1024.0.pow(digitGroups.toDouble()).roundToInt()} ${units[digitGroups]}"
}
