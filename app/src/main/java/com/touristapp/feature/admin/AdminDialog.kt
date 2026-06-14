package com.touristapp.feature.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.touristapp.feature.admin.AdminViewModel
@Composable
fun AdminDialog(
    onApartmentSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onLockout: () -> Unit,
    isKioskEnabled: Boolean,
    onExitKiosk: () -> Unit,
    onEnableKiosk: () -> Unit
) {
    val viewModel: AdminViewModel = hiltViewModel()
    // Clear any prior session each time the dialog opens so login is always required.
    LaunchedEffect(Unit) { viewModel.reset() }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            AdminLoginFlow(
                viewModel = viewModel,
                onApartmentSelected = onApartmentSelected,
                onDismiss = onDismiss,
                onLockout = onLockout,
                isKioskEnabled = isKioskEnabled,
                onExitKiosk = onExitKiosk,
                onEnableKiosk = onEnableKiosk,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
