package com.touristapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Dialog overlay for admin reconfiguration.
 * Minimal — no title, no explanation. Just login fields.
 */
@Composable
fun AdminDialog(
    onApartmentSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onLockout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            AdminLoginFlow(
                onApartmentSelected = onApartmentSelected,
                onDismiss = onDismiss,
                onLockout = onLockout,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
