package team.mke.utils

import java.time.format.DateTimeFormatter
import java.util.Locale

val shortDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))
val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.forLanguageTag("ru"))
val shortDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", Locale.forLanguageTag("ru"))
val hoursFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH", Locale.forLanguageTag("ru"))
val minutesFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("mm", Locale.forLanguageTag("ru"))