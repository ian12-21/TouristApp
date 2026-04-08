package com.touristapp.ui.screens.apartment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.touristapp.data.model.Apartment
import com.touristapp.data.model.Room
// import com.touristapp.data.model.CustomSection

sealed class ApartmentSection(val title: String, val icon: ImageVector) {
    data object Overview : ApartmentSection("Overview", Icons.Default.Home)
    data object Rooms : ApartmentSection("Rooms & Appliances", Icons.Default.Star)
    data object HouseRules : ApartmentSection("House Rules", Icons.Default.Info)
    data object Checkout : ApartmentSection("Checkout", Icons.Default.ExitToApp)
    data object Transport : ApartmentSection("Transport", Icons.Default.DirectionsBus)
    // data class Custom(val section: CustomSection) : ApartmentSection(section.title, Icons.Default.Article)
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
    rooms: List<Room> = emptyList(),
    apartmentName: String,
    onBack: () -> Unit
    // customSections: List<CustomSection> = emptyList()
) {
    var selectedSection by remember { mutableStateOf<ApartmentSection>(ApartmentSection.Overview) }

    val sections = buildList {
        addAll(fixedSections)
        // customSections.forEach { add(ApartmentSection.Custom(it)) }
    }

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
                when (val current = selectedSection) {
                    is ApartmentSection.Overview -> OverviewContent(apartment)
                    is ApartmentSection.Rooms -> RoomsContent(rooms)
                    is ApartmentSection.HouseRules -> HouseRulesContent(apartment)
                    is ApartmentSection.Checkout -> CheckoutContent(apartment)
                    is ApartmentSection.Transport -> TransportContent(apartment)
                    // is ApartmentSection.Custom -> CustomSectionContent(current.section)
                }
            }
        }
    }
}

@Composable
private fun SidebarItem(
    section: ApartmentSection,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = section.icon,
            contentDescription = section.title,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = section.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
    }
}

// ── Section content composables ──

@Composable
private fun OverviewContent(apartment: Apartment) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = apartment.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Street: ${apartment.address}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (apartment.description.isNotBlank()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Text(
                text = apartment.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Details grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (apartment.size.isNotBlank()) {
                DetailRow(label = "Size", value = "${apartment.size} m²")
            }
            if (apartment.capacity > 0) {
                DetailRow(label = "Capacity", value = "${apartment.capacity} guests")
            }
            if (apartment.renovationYear > 0) {
                DetailRow(label = "Renovated", value = apartment.renovationYear.toString())
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RoomsContent(rooms: List<Room>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Rooms & Appliances",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (rooms.isEmpty()) {
            Text(
                text = "No rooms listed.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(rooms) { room ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = room.id,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            room.appliances.forEach { (name, appliance) ->
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = appliance.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (appliance.instructions.isNotEmpty()) {
                                        Text(
                                            text = appliance.instructions,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HouseRulesContent(apartment: Apartment) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "House Rules",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (apartment.houseRules.isEmpty()) {
            Text(
                text = "No house rules listed.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            apartment.houseRules.forEachIndexed { index, rule ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = rule,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckoutContent(apartment: Apartment) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Checkout",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (apartment.checkoutTime.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Checkout time",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Checkout Time",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = apartment.checkoutTime,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (apartment.checkoutInstructions.isNotBlank()) {
            Text(
                text = apartment.checkoutInstructions,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TransportContent(apartment: Apartment) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Transport",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (apartment.transportTips.isNotBlank()) {
            Text(
                text = apartment.transportTips,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = "No transport tips available.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Custom section content (commented out) ──

// @Composable
// private fun CustomSectionContent(section: CustomSection) {
//     Column(
//         modifier = Modifier
//             .fillMaxSize()
//             .verticalScroll(rememberScrollState()),
//         verticalArrangement = Arrangement.spacedBy(16.dp)
//     ) {
//         Text(
//             text = section.title,
//             style = MaterialTheme.typography.headlineMedium,
//             color = MaterialTheme.colorScheme.onSurface
//         )
//         Text(
//             text = section.content,
//             style = MaterialTheme.typography.bodyLarge,
//             color = MaterialTheme.colorScheme.onSurface
//         )
//     }
// }
