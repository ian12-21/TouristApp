package com.touristapp.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.touristapp.core.i18n.ProvideLocalizedContext

/**
 * A [Dialog] that re-applies the selected app language to its content.
 *
 * Dialogs render in a separate window with their own composition root, which re-provides the
 * platform `LocalContext`/`LocalConfiguration` from the Activity — discarding the app-root locale
 * override, so `stringResource()` inside a raw [Dialog] falls back to the launch-time language.
 * Wrapping the content in [ProvideLocalizedContext] repairs that across the window boundary, and
 * doing it here means every dialog gets it for free — there's no per-dialog boilerplate to forget.
 *
 * Use this instead of [Dialog] for any dialog that shows localized text.
 *
 * @param forceLanguage pins the dialog to a specific language code (e.g. an English-only admin
 *   surface); when null the dialog follows the app's currently selected language.
 */
@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    forceLanguage: String? = null,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        if (forceLanguage != null) {
            ProvideLocalizedContext(language = forceLanguage, content = content)
        } else {
            ProvideLocalizedContext(content = content)
        }
    }
}
