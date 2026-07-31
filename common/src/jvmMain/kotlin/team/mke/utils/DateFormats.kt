package team.mke.utils

import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * `dd.MM.yyyy`
 *
 * Example: `15.05.2026`
 * */
val shortDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

/**
 * `d MMMM yyyy`
 *
 * Example: `15 may 2026`
 * */
val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))

/**
 * `d MMMM yyyy, HH:mm`
 *
 * Example: `15 may 2026, 14:30`
 * */
val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.forLanguageTag("ru"))

/**
 * `dd.MM.yyyy, HH:mm`
 *
 * Example: `15.05.2026, 14:30`
 * */
val shortDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", Locale.forLanguageTag("ru"))

/**
 * `HH:mm`
 *
 * Example: `14:30`
 * */
val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru"))

/**
 * `HH`
 *
 * Example: `14`
 * */
val hoursFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH", Locale.forLanguageTag("ru"))

/**
 * `mm`
 *
 * Example: `30`
 * */
val minutesFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("mm", Locale.forLanguageTag("ru"))
