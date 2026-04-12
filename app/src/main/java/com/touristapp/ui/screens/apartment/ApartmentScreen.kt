package com.touristapp.ui.screens.apartment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.touristapp.data.model.Apartment
import com.touristapp.data.model.Room
import com.touristapp.data.model.Stay
import com.touristapp.data.model.TransportationService
import com.touristapp.data.repository.TouristRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

sealed class ApartmentSection(val title: String, val icon: ImageVector) {
    data object Overview : ApartmentSection("Overview", Icons.Default.Home)
    data object Rooms : ApartmentSection("Rooms & Appliances", Icons.Default.MeetingRoom)
    data object HouseRules : ApartmentSection("House Rules", Icons.Default.Rule)
    data object Checkout : ApartmentSection("Checkout", Icons.Default.ExitToApp)
    data object Transport : ApartmentSection("Transport", Icons.Default.DirectionsBus)
}

private val fixedSections = listOf(
    ApartmentSection.Overview,
    ApartmentSection.Rooms,
    ApartmentSection.HouseRules,
    ApartmentSection.Checkout,
    ApartmentSection.Transport
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentScreen(
    apartment: Apartment?,
    apartmentName: String,
    currentStay: Stay? = null,
    repository: TouristRepository,
    onBack: () -> Unit
) {
    var selectedSection by remember { mutableStateOf<ApartmentSection>(ApartmentSection.Overview) }

    // Lazy-loaded data: fetched once when ApartmentScreen is first composed
    var rooms by remember { mutableStateOf<List<Room>>(emptyList()) }
    var transportationServices by remember { mutableStateOf<List<TransportationService>>(emptyList()) }

    LaunchedEffect(apartment?.id) {
        val id = apartment?.id ?: return@LaunchedEffect
        coroutineScope {
            launch { rooms = repository.getRooms(id) }
            launch {
                val privateIds = apartment.transportation
                    .filter { it.type == "private" && it.transportationId.isNotBlank() }
                    .map { it.transportationId }
                transportationServices = repository.getTransportationServices(privateIds)
            }
        }
    }

    val sections = fixedSections

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    title = {
                        Text(
                            text = apartmentName,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(40.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    ) { innerPadding ->
        if (apartment == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Left sidebar
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.25f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(sections) { section ->
                    val isSelected = selectedSection == section
                    SidebarItem(
                        section = section,
                        isSelected = isSelected,
                        onClick = { selectedSection = section }
                    )
                }
            }

            // Right content area
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(24.dp)
            ) {
                when (selectedSection) {
                    is ApartmentSection.Overview -> OverviewContent(apartment)
                    is ApartmentSection.Rooms -> RoomsContent(rooms)
                    is ApartmentSection.HouseRules -> HouseRulesContent(apartment)
                    is ApartmentSection.Checkout -> CheckoutContent(apartment, currentStay)
                    is ApartmentSection.Transport -> TransportContent(apartment, transportationServices)
                }
            }
        }
    }
}
