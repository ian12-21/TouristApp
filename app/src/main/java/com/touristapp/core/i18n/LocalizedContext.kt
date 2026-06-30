package com.touristapp.core.i18n

import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * The app's currently selected language code. Provided once at the app root and read by
 * [ProvideLocalizedContext]. Unlike the platform [LocalContext]/[LocalConfiguration], a custom
 * CompositionLocal propagates into Dialog/Popup sub-compositions, so dialogs can recover the
 * selected language even though they host content in a separate window.
 */
val LocalAppLanguage = staticCompositionLocalOf { DEFAULT_LANGUAGE }

/**
 * Re-resolves [LocalContext] and [LocalConfiguration] to [language] so every `stringResource()` and
 * locale-aware API under [content] uses the selected language — without an Activity recreate.
 *
 * Apply this at the app root, and again inside every `Dialog`/`Popup`: those host their content in a
 * separate [androidx.compose.ui.platform.AndroidComposeView] that re-provides the platform
 * [LocalContext]/[LocalConfiguration] from the Activity (i.e. the launch-time locale), so the root
 * override is otherwise lost across the window boundary.
 *
 * The Activity is kept as the wrapper's base context: `stringResource()` reads strings from
 * `LocalContext.current.resources` (overridden here with the localized resources), while
 * `hiltViewModel()` unwraps the ContextWrapper chain to find the Activity.
 */
@Composable
fun ProvideLocalizedContext(
    language: String = LocalAppLanguage.current,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val localizedContext = remember(language, baseContext) {
        val config = Configuration(baseContext.resources.configuration).apply {
            setLocale(Locale(language))
        }
        val localizedResources = baseContext.createConfigurationContext(config).resources
        object : ContextWrapper(baseContext) {
            override fun getResources(): Resources = localizedResources
        }
    }
    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        content = content
    )
}
