package com.touristapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Rule
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
            .padding(24.dp)
    ) {
        // Welcome + description centered vertically in available space
        val welcomeText = currentStay?.welcomeMessage?.takeIf { it.isNotBlank() }
            ?.let { "Welcome $it" }
            ?: "Welcome!"

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = welcomeText,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            apartment?.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Quick action cards pinned to bottom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Lock,
                label = "WiFi",
                modifier = Modifier.weight(1f),
                onClick = { showWifiDialog = true }
            )
            QuickActionCard(
                icon = Icons.Default.Rule,
                label = "Rules",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
            QuickActionCard(
                icon = Icons.Default.Phone,
                label = "SOS",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
            QuickActionCard(
                icon = Icons.Default.Star,
                label = "Check-out",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
            QuickActionCard(
                icon = Icons.Default.DirectionsBus,
                label = "Transport",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
            QuickActionCard(
                icon = Icons.Default.Info,
                label = "About",
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
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
        }
    }
}
