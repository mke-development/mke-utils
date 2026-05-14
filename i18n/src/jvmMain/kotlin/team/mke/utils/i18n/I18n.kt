package team.mke.utils.i18n

import com.ibm.icu.text.MessageFormat
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

internal object I18n {
    private val bundleNames = mutableListOf(
        "messages.messages"
    )

    private val defaultLocale = Locale.ENGLISH

    fun addBundle(bundle: String) {
        bundleNames.add(bundle)
    }

    internal operator fun get(key: String, locale: Locale, vararg args: Any?): String {
        for(bundleName in bundleNames) {
            val bundle = ResourceBundle.getBundle(bundleName, locale)
            if (bundle.containsKey(key)) {
                val pattern = bundle.getString(key)
                return MessageFormat(pattern, locale).format(args)
            }
        }

        val bundlesString = bundleNames.joinToString()
        throw MissingResourceException(
            "Can't find resource for bundle $bundlesString, key $key",
            bundlesString,
            key,
        )
    }
}
