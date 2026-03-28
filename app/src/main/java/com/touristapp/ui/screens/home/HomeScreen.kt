package com.touristapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.touristapp.data.model.Apartment as ApartmentModel
import com.touristapp.data.model.Contact
import com.touristapp.data.model.Stay
import com.touristapp.ui.components.ApartmentInfoDialog
import com.touristapp.ui.components.ContactsDialog
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeSlide(apartment: ApartmentModel?, currentStay: Stay?, emergencyContacts: List<Contact> = emptyList()) {
    var showApartmentDialog by remember { mutableStateOf(false) }
    var showContactsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        val welcomeText = currentStay?.welcomeMessage?.takeIf { it.isNotBlank() }
            ?.let { "Welcome $it" }
            ?: "Welcome!"

        Spacer(modifier = Modifier.weight(1f))

        // Welcome message
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = welcomeText,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // WiFi info row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "WiFi",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Wi-Fi: ${apartment?.wifiName?.takeIf { it.isNotBlank() } ?: "Not configured"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        apartment?.wifiPassword?.takeIf { it.isNotBlank() }?.let { password ->
                            Text(
                                text = "Password: $password",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Checkout info row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Checkout",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        val checkoutTime = apartment?.checkoutTime?.takeIf { it.isNotBlank() }
                        val checkoutDate = currentStay?.checkOut?.let {
                            SimpleDateFormat("MMM d", Locale.getDefault()).format(it.toDate())
                        }
                        Text(
                            text = when {
                                checkoutTime != null && checkoutDate != null -> "Checkout by $checkoutTime on $checkoutDate"
                                checkoutTime != null -> "Checkout by $checkoutTime"
                                checkoutDate != null -> "Checkout on $checkoutDate"
                                else -> "Checkout info not set"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Apartment contacts
            val apartmentContacts = apartment?.contacts ?: emptyList()
            if (apartmentContacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Contacts",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    apartmentContacts.forEach { contact ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1
                                )
                                Text(
                                    text = contact.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Apartment,
                label = "Apartment",
                modifier = Modifier.weight(1f),
                onClick = { showApartmentDialog = true }
            )
            QuickActionCard(
                icon = Icons.Default.ShoppingCart,
                label = "Stores",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
            QuickActionCard(
                icon = Icons.Default.BeachAccess,
                label = "Beaches",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
            QuickActionCard(
                icon = Icons.Default.Restaurant,
                label = "Restaurants",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
            QuickActionCard(
                icon = Icons.Default.RateReview,
                label = "Review",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
            QuickActionCard(
                icon = Icons.Default.Phone,
                label = "Emergency",
                modifier = Modifier.weight(1f),
                onClick = { showContactsDialog = true }
            )
        }
    }

    if (showApartmentDialog && apartment != null) {
        ApartmentInfoDialog(
            apartment = apartment,
            onDismiss = { showApartmentDialog = false }
        )
    }

    if (showContactsDialog) {
        ContactsDialog(
            emergencyContacts = emergencyContacts,
            onDismiss = { showContactsDialog = false }
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
    val cardShape = RoundedCornerShape(20.dp)
    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = 4.dp,
                shape = cardShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
        }
    }
}
