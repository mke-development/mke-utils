package team.mke.utils.ktor.server.ext.statuspages

import io.ktor.http.*
import io.ktor.i18n.locale
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import team.mke.utils.db.EntityNotFoundException
import team.mke.utils.db.Gender
import team.mke.utils.db.entityGender
import team.mke.utils.db.entityName
import team.mke.utils.ktor.server.ext.respondError
import team.mke.utils.locale.ruLocale
import java.util.*

/**
 * Returns the [Locale] for this call based on the `Accept-Language` header
 */
val ApplicationCall.locale: Locale
    get() {
        return request.headers[HttpHeaders.AcceptLanguage]?.let { languageHeader ->
            runCatching {
                Locale.LanguageRange.parse(languageHeader)
                    .firstOrNull()
                    ?.let { Locale.forLanguageTag(it.range) }
            }.getOrNull()
        } ?: locale
    }

private fun buildRussianMessage(e: EntityNotFoundException, locale: Locale): String {
    val name = e.entity.entityName(locale) ?: "Сущность"
    val ending = when (e.entity.entityGender(locale) ?: e.entity.entityGender(ruLocale)) {
        Gender.MASCULINE -> "найден"
        Gender.FEMININE -> "найдена"
        Gender.NEUTER -> "найдено"
        null -> "найдена"
    }
    return "$name с id ${e.id} не $ending"
}

private fun buildEnglishMessage(e: EntityNotFoundException, locale: Locale): String {
    val name = e.entity.entityName(locale) ?: "Entity"
    return "$name with id ${e.id} not found"
}

/**
 * Handles [EntityNotFoundException] by responding with HTTP 404 (Not Found).
 *
 * It first invokes [messageSupplier] (if provided). If [messageSupplier] returns a non-null message,
 * that message is used. Otherwise, it falls back to built-in translations ("en", "ru") or default Russian message.
 *
 * @param messageSupplier Optional lambda to provide custom messages based on call and exception.
 */
fun StatusPagesConfig.handleEntityNotFoundException(
    messageSupplier: ((call: ApplicationCall, e: EntityNotFoundException) -> String?)? = null
) {
    exception<EntityNotFoundException> { call, e ->
        if (!call.isHandled) {
            val locale = call.locale

            val message = messageSupplier?.invoke(call, e) ?: e.message ?: when (locale.language) {
                "en" -> buildEnglishMessage(e, locale)
                "ru" -> buildRussianMessage(e, locale)
                else -> buildRussianMessage(e, ruLocale)
            }

            call.respondError(message, status = HttpStatusCode.NotFound)
        }
    }
}
