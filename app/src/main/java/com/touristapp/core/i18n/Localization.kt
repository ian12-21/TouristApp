package com.touristapp.core.i18n

/**
 * The four languages the app can display. The guest app shows one language at a
 * time (with an English fallback) — unlike the admin web app, which edits all
 * four side by side.
 */
enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    EN("en", "English", "🇬🇧"),
    HR("hr", "Hrvatski", "🇭🇷"),
    IT("it", "Italiano", "🇮🇹"),
    DE("de", "Deutsch", "🇩🇪");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: EN
    }
}

/** Default language used everywhere a value is missing. */
const val DEFAULT_LANGUAGE = "en"

/**
 * Resolve a possibly-localized Firestore value to a single string for [lang].
 *
 * Content written by the admin web app is stored as a `{ en, hr, it, de }` map,
 * but legacy/unmigrated documents may still hold a plain string. Mirrors the web
 * app's `localizeValue`: prefer the requested language, then English, then the
 * first non-blank value; plain strings pass through unchanged.
 */
fun localize(raw: Any?, lang: String): String = when (raw) {
    is String -> raw
    is Map<*, *> -> {
        val requested = (raw[lang] as? String)?.takeIf { it.isNotBlank() }
        val english = (raw[DEFAULT_LANGUAGE] as? String)?.takeIf { it.isNotBlank() }
        requested
            ?: english
            ?: raw.values.filterIsInstance<String>().firstOrNull { it.isNotBlank() }
            ?: ""
    }
    else -> ""
}

/**
 * Resolve a list whose elements may each be a plain string or a localized map
 * (e.g. house-rule `rules` arrays). Blank results are dropped.
 */
fun localizeList(raw: Any?, lang: String): List<String> =
    (raw as? List<*>)?.mapNotNull { element ->
        localize(element, lang).takeIf { it.isNotBlank() }
    } ?: emptyList()
