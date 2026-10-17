package com.touristapp.core.i18n

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * A date format in the language picked in the app, for a [skeleton] such as `"dMMM"`.
 * Reads the locale from [LocalConfiguration], which [ProvideLocalizedContext] sets;
 * `Locale.getDefault()` is the device locale and ignores the in-app picker. The skeleton
 * becomes each language's own pattern: "24 Oct" in English, "24. lis" in Croatian.
 */
@Composable
fun rememberLocalizedDateFormat(skeleton: String): SimpleDateFormat {
    val appLocale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    // Plain "en" formats dates the US way ("Oct 24"); the guests are in Europe.
    val locale = if (appLocale.language == ENGLISH && appLocale.country.isEmpty()) Locale.UK else appLocale
    return remember(locale, skeleton) {
        SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
    }
}

private const val ENGLISH = "en"
