package team.mke.utils.ext

/**
 * Данные формата телефона.
 *
 * @param countryCode Код страны
 * @param phoneLength Длина номера телефона без учета кода страны
 * @param resultCountryCode Отформатированный код страны возвращаемый [exportPhones]
 * */
data class PhoneFormat(val countryCode: Int, val phoneLength: Int, val resultCountryCode: String = "+$countryCode")

/** Список форматов (российские номера, начинающиеся с 7 и 8) используемых в [exportPhones] по умолчанию. */
val defaultExportPhonesFormats = arrayOf(
    // russian
    PhoneFormat(7, 10, "+7"),
    PhoneFormat(8, 10, "+7"),

    //
)

/**
 * Извлекает номера телефонов по данным [форматов][formats].
 *
 * @sample team.mke.utils.test.ExportPhonesTests
 * */
fun String.exportPhones(
    vararg formats: PhoneFormat = defaultExportPhonesFormats,
): List<String> {
    val fixed = replace("\\D".toRegex(), "")
    val regex = Regex("(${formats.joinToString("|") { "${it.countryCode}\\d{${it.phoneLength}}" }})")

    return regex.findAll(fixed)
        .map {
            val format = formats.first { f ->
                it.value.startsWith(f.countryCode.toString()) &&
                it.value.length == (f.countryCode.toString().length + f.phoneLength)
            }
            it.value.replace("^${format.countryCode}".toRegex(), format.resultCountryCode)
        }
        .toList()
}