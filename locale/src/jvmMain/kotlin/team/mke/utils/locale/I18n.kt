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
        val pattern = bundle(locale).getString(key)
        return MessageFormat(pattern, locale).format(args)
    }
}