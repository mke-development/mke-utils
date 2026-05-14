package team.mke.utils.i18n

import java.util.Locale

fun i18n(key: String, locale: Locale, vararg args: Any?) = I18n.get(key, locale, *args)

context(localeCtx: LocaleContext)
fun i18n(key: String, vararg args: Any?) = I18n.get(key, localeCtx.locale, *args)
