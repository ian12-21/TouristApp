package com.touristapp.ui.screens.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.touristapp.ui.components.AdminDialog

/**
 * Main guest-facing screen.
 * Shows apartment info, welcome message, etc.
 * Contains hidden 10-second long-press trigger for admin access.
 */
@Composable
fun HomeScreen(apartmentId: String, onReconfigure: () -> Unit) {
    var showAdminDialog by remember { mutableStateOf(false) }
    var cooldownUntil by remember { mutableLongStateOf(0L) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Hidden long-press area — top of screen, invisible to tourists
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                val pressStartTime = System.currentTimeMillis()
                                val released = tryAwaitRelease()
                                val pressDuration = System.currentTimeMillis() - pressStartTime

                                if (released && pressDuration >= 10_000) {
                                    if (System.currentTimeMillis() > cooldownUntil) {
                                        showAdminDialog = true
                                    }
                                    // If in cooldown, silently ignore
                                }
                            }
                        )
                    }
            )

            // TODO: Replace with real apartment data
            Text(
                text = "Welcome!",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Apartment: $apartmentId",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "This is the guest-facing home screen. We'll build this out with apartment details, WiFi info, and the welcome message.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Admin dialog overlay
        if (showAdminDialog) {
            AdminDialog(
                onApartmentSelected = { newId ->
                    showAdminDialog = false
                    onReconfigure()
                },
                onDismiss = { showAdminDialog = false },
                onLockout = {
                    cooldownUntil = System.currentTimeMillis() + 60_000
                    showAdminDialog = false
                }
            )
        }
    }
}
