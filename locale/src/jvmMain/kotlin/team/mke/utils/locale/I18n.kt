package team.mke.utils.locale

import java.text.MessageFormat
import java.util.Locale
import java.util.ResourceBundle

object I18n {
    var bundle = "messages.messages"
    var defaultLocale = ruLocale

    private val cache = mutableMapOf<Locale, ResourceBundle>()

    private fun bundle(locale: Locale = defaultLocale): ResourceBundle = cache[locale] ?: run {
        val bundle = ResourceBundle.getBundle(bundle, locale)
        cache[locale] = bundle
        bundle
    }

    internal operator fun get(key: String, locale: Locale = defaultLocale, vararg args: Any?): String {
        val bundle = runCatching { bundle(locale) }.getOrNull()
        val pattern = if (bundle != null && bundle.containsKey(key)) {
            bundle.getString(key)
        } else {
            key
        }

        return runCatching { MessageFormat(pattern, locale).format(args) }.getOrDefault(key)
    }
}
