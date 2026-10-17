package com.touristapp.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/**
 * A review score with one decimal in the language picked in the app: "6.5" in English,
 * "6,5" in Croatian, Italian and German. `String.format` without a locale would use the
 * device locale and ignore the in-app picker (see [rememberLocalizedDateFormat]).
 */
@Composable
fun formatScore(score: Double): String {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    return String.format(locale, SCORE_FORMAT, score)
}

private const val SCORE_FORMAT = "%.1f"
