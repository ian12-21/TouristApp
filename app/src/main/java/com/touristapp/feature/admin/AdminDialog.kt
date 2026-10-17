package com.touristapp.feature.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.touristapp.core.ui.components.AppDialog
import com.touristapp.data.model.Pairing
import com.touristapp.feature.admin.AdminViewModel
@Composable
fun AdminDialog(
    onApartmentSelected: (Pairing) -> Unit,
    onDismiss: () -> Unit,
    onLockout: () -> Unit,
    isKioskEnabled: Boolean,
    canUseKiosk: Boolean,
    onExitKiosk: () -> Unit,
    onEnableKiosk: () -> Unit,
    pairedApartmentId: String? = null
) {
    val viewModel: AdminViewModel = hiltViewModel()
    LaunchedEffect(Unit) { viewModel.lock() }
    // Closing with X keeps the tablet as it was, and must not leave the owner signed in.
    DisposableEffect(Unit) { onDispose { viewModel.lock() } }
    // Admin is an owner-facing, English-only surface.
    AppDialog(onDismissRequest = onDismiss, forceLanguage = "en") {
        Card(
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            AdminLoginFlow(
                viewModel = viewModel,
                onApartmentSelected = onApartmentSelected,
                onDismiss = onDismiss,
                onLockout = onLockout,
                isKioskEnabled = isKioskEnabled,
                canUseKiosk = canUseKiosk,
                onExitKiosk = onExitKiosk,
                onEnableKiosk = onEnableKiosk,
                pairedApartmentId = pairedApartmentId,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
