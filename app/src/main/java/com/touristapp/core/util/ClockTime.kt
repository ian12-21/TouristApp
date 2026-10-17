package com.touristapp.core.util

import java.util.Locale

/**
 * `(h)h`, optional `:mm` or `.mm`, optional AM/PM in any of the usual spellings
 * ("am", "AM", "a.m.").
 */
private val CLOCK_TIME = Regex("""(\d{1,2})(?:[:.](\d{2}))?\s*(?:([ap])\.?\s*m\.?)?""", RegexOption.IGNORE_CASE)

private const val HOURS_PER_DAY = 24
private const val HOURS_PER_HALF_DAY = 12
private const val MINUTES_PER_HOUR = 60

/**
 * The apartment's checkout time is free text (SCHEMA.md §3.1); the admin used to suggest
 * "10:00 AM" while owners also type "11:00". Rewrites anything that reads as a clock time
 * to 24-hour `HH:mm`, the format the tablet uses for every other time, and leaves other
 * text as the owner wrote it.
 */
fun normalizeClockTime(text: String): String {
    val trimmed = text.trim()
    val match = CLOCK_TIME.matchEntire(trimmed) ?: return trimmed
    val (hourText, minuteText, meridiem) = match.destructured
    val minute = minuteText.ifEmpty { "0" }.toInt()
    if (minute >= MINUTES_PER_HOUR) return trimmed
    val hour = hourText.toInt()
    val hour24 = when {
        meridiem.isEmpty() -> hour.takeIf { it < HOURS_PER_DAY }
        hour !in 1..HOURS_PER_HALF_DAY -> null
        meridiem.equals("p", ignoreCase = true) -> hour % HOURS_PER_HALF_DAY + HOURS_PER_HALF_DAY
        else -> hour % HOURS_PER_HALF_DAY
    } ?: return trimmed
    return String.format(Locale.ROOT, "%02d:%02d", hour24, minute)
}
