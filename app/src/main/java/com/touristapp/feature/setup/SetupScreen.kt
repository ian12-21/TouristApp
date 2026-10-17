package com.touristapp.feature.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.touristapp.data.model.Pairing
import com.touristapp.feature.admin.AdminViewModel
import com.touristapp.feature.admin.AdminLoginFlow
@Composable
fun SetupScreen(
    onApartmentSelected: (Pairing) -> Unit
) {
    val viewModel: AdminViewModel = hiltViewModel()
    // AdminViewModel is activity-scoped, so this is the same instance the admin
    // dialog uses. Resetting on entry — as AdminDialog does — guarantees setup
    // always starts at the password prompt and can never inherit an
    // isAuthenticated=true left over from an earlier admin session.
    LaunchedEffect(Unit) { viewModel.lock() }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // The app is edge-to-edge, so the keyboard doesn't shrink the window: imePadding keeps the
        // card above it, or it covers the password field and the button on a landscape tablet.
        // Below API 30 the inset only arrives with adjustResize (set in the manifest).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .padding(24.dp)
            ) {
                AdminLoginFlow(
                    viewModel = viewModel,
                    onApartmentSelected = onApartmentSelected,
                    onDismiss = null,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}
