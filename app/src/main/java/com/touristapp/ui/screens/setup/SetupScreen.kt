package com.touristapp.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.touristapp.ui.components.AdminLoginFlow

/**
 * Full-screen setup shown on first launch.
 * Owner logs in, picks an apartment, then the tablet enters kiosk mode.
 */
@Composable
fun SetupScreen(onApartmentSelected: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .padding(24.dp)
            ) {
                AdminLoginFlow(
                    onApartmentSelected = onApartmentSelected,
                    onDismiss = null, // No dismiss on first setup
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}
