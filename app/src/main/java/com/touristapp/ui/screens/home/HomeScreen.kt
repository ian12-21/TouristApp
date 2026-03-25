package com.touristapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.touristapp.data.model.Apartment
import com.touristapp.data.model.Stay
import com.touristapp.ui.components.WifiDialog

@Composable
fun HomeSlide(apartment: Apartment?, currentStay: Stay?) {
    var showWifiDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Welcome message from active stay
        val welcomeText = currentStay?.welcomeMessage?.takeIf { it.isNotBlank() }
            ?.let { "Welcome $it" }
            ?: "Welcome!"

        Text(
            text = welcomeText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Quick action buttons grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Lock,
                label = "WiFi",
                modifier = Modifier.weight(1f),
                onClick = { showWifiDialog = true }
            )
            QuickActionCard(
                icon = Icons.Default.Info,
                label = "House Rules",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Phone,
                label = "Emergency",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
            QuickActionCard(
                icon = Icons.Default.Star,
                label = "Check-out",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
        }
    }

    if (showWifiDialog) {
        WifiDialog(
            wifiName = apartment?.wifiName ?: "",
            wifiPassword = apartment?.wifiPassword ?: "",
            onDismiss = { showWifiDialog = false }
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
