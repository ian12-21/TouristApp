package com.touristapp.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.touristapp.R
import com.touristapp.core.i18n.AppLanguage
import com.touristapp.core.i18n.ProvideLocalizedContext

/**
 * A simple popup to choose the app language. Highlights the active language and
 * reports the chosen code; persistence and locale switching happen in the caller.
 */
@Composable
fun LanguagePickerDialog(
    currentLanguage: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ProvideLocalizedContext {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = stringResource(R.string.language_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                AppLanguage.entries.forEach { language ->
                    LanguageRow(
                        language = language,
                        selected = language.code == currentLanguage,
                        onClick = {
                            onSelect(language.code)
                            onDismiss()
                        }
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun LanguageRow(
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = language.flag, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = language.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
