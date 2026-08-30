package team.mke.utils.i18n

import java.util.Locale

fun i18n(key: String, locale: Locale, vararg args: Any?) = I18n.get(key, locale, *args)

context(localeCtx: LocaleContext)
fun i18n(key: String, vararg args: Any?) = I18n.get(key, localeCtx.locale, *args)

fun i18nUnsafe(key: String, locale: Locale, vararg args: Any?) = I18n.getUnsafe(key, locale, *args)

context(localeCtx: LocaleContext)
fun i18nUnsafe(key: String, vararg args: Any?) = I18n.getUnsafe(key, localeCtx.locale, *args)
