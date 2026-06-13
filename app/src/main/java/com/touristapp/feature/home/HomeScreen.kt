package com.touristapp.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touristapp.R
import com.touristapp.core.ui.components.ContactsDialog
import com.touristapp.core.ui.components.appliancePalette
import com.touristapp.data.model.Apartment as ApartmentModel
import com.touristapp.data.model.Contact
import com.touristapp.data.model.Stay
import com.touristapp.feature.places.PlaceCategory
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeSlide(
    apartment: ApartmentModel?,
    currentStay: Stay?,
    cachedEmergencyContacts: List<Contact> = emptyList(),
    onNavigateToReviews: () -> Unit = {},
    onNavigateToApartment: () -> Unit = {},
    onNavigateToHouseRules: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    onNavigateToCategory: (PlaceCategory) -> Unit = {}
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showContactsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cachedEmergencyContacts) {
        viewModel.seed(cachedEmergencyContacts)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        val welcomeText = currentStay?.welcomeMessage?.takeIf { it.isNotBlank() }
            ?.let { stringResource(R.string.home_welcome_with_name, it) }
            ?: stringResource(R.string.home_welcome)

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = welcomeText,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp),
                color = MaterialTheme.colorScheme.onBackground
            )

            apartment?.welcomeMessage?.takeIf { it.isNotBlank() }?.let { msg ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        contentDescription = stringResource(R.string.cd_wifi),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        val wifiName = apartment?.wifiName?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.home_wifi_not_configured)
                        Text(
                            text = stringResource(R.string.home_wifi_label, wifiName),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        apartment?.wifiPassword?.takeIf { it.isNotBlank() }?.let { password ->
                            Text(
                                text = stringResource(R.string.home_wifi_password, password),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                        contentDescription = stringResource(R.string.cd_checkout),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        val checkoutTime = apartment?.checkoutTime?.takeIf { it.isNotBlank() }
                        val checkoutDate = currentStay?.checkOut?.let {
                            SimpleDateFormat("d MMM", Locale.getDefault()).format(it.toDate())
                        }
                        val text = when {
                            checkoutTime != null && checkoutDate != null ->
                                stringResource(R.string.home_checkout_by_time_and_date, checkoutTime, checkoutDate)
                            checkoutTime != null ->
                                stringResource(R.string.home_checkout_by_time, checkoutTime)
                            checkoutDate != null ->
                                stringResource(R.string.home_checkout_on_date, checkoutDate)
                            else -> stringResource(R.string.home_checkout_not_set)
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            val apartmentContacts = apartment?.contacts ?: emptyList()
            if (apartmentContacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.home_contacts),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    apartmentContacts.forEach { contact ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Info,
                label = stringResource(R.string.home_action_apartment),
                accentColor = appliancePalette[0],
                modifier = Modifier.weight(1f),
                onClick = onNavigateToApartment
            )
            QuickActionCard(
                icon = Icons.Default.Explore,
                label = stringResource(R.string.home_action_explore),
                accentColor = appliancePalette[1],
                modifier = Modifier.weight(1f),
                onClick = onNavigateToExplore
            )
            QuickActionCard(
                icon = Icons.Default.Rule,
                label = stringResource(R.string.home_action_house_rules),
                accentColor = appliancePalette[2],
                modifier = Modifier.weight(1f),
                onClick = onNavigateToHouseRules
            )
            QuickActionCard(
                icon = Icons.Default.BeachAccess,
                label = stringResource(R.string.home_action_beaches),
                accentColor = appliancePalette[3],
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToCategory(PlaceCategory.BEACH) }
            )
            QuickActionCard(
                icon = Icons.Default.RateReview,
                label = stringResource(R.string.home_action_review),
                accentColor = appliancePalette[4],
                modifier = Modifier.weight(1f),
                onClick = onNavigateToReviews
            )
            QuickActionCard(
                icon = Icons.Default.Phone,
                label = stringResource(R.string.home_action_emergency),
                accentColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
                onClick = {
                    showContactsDialog = true
                    viewModel.loadEmergencyContacts()
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }

    if (showContactsDialog) {
        ContactsDialog(
            emergencyContacts = state.emergencyContacts,
            onDismiss = { showContactsDialog = false },
            isLoading = state.isLoadingContacts,
            errorMessage = state.contactsError,
            onRetry = { viewModel.loadEmergencyContacts() }
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)
    Card(
        onClick = onClick,
        modifier = modifier
            .height(118.dp)
            .shadow(
                elevation = 4.dp,
                shape = cardShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
                .padding(horizontal = 6.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
        }
    }
}
