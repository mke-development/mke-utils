package team.mke.utils.locale

import java.util.Locale
import kotlin.text.get

fun i18n(key: String, locale: Locale, vararg args: Any?) = I18n.get(key, locale, *args)

context(ctx: LocaleContext)
fun i18n(key: String, vararg args: Any?) = I18n.get(key, ctx.locale, *args)
