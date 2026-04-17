package com.touristapp.feature.apartment

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touristapp.R
import com.touristapp.core.ui.components.ErrorContent
import com.touristapp.data.model.Apartment
import com.touristapp.data.model.Stay

sealed class ApartmentSection(@StringRes val titleRes: Int, val icon: ImageVector) {
    data object Overview : ApartmentSection(R.string.apartment_section_overview, Icons.Default.Home)
    data object Rooms : ApartmentSection(R.string.apartment_section_rooms, Icons.Default.MeetingRoom)
    data object HouseRules : ApartmentSection(R.string.apartment_section_rules, Icons.Default.Rule)
    data object Checkout : ApartmentSection(R.string.apartment_section_checkout, Icons.Default.ExitToApp)
    data object Transport : ApartmentSection(R.string.apartment_section_transport, Icons.Default.DirectionsBus)
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
    onBack: () -> Unit
) {
    val viewModel: ApartmentViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(apartment?.id) {
        val id = apartment?.id ?: return@LaunchedEffect
        viewModel.loadData(id, apartment.transportation)
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
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.25f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(fixedSections) { section ->
                    SidebarItem(
                        section = section,
                        isSelected = state.selectedSection == section,
                        onClick = { viewModel.selectSection(section) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(24.dp)
            ) {
                if (state.error != null) {
                    ErrorContent(
                        message = state.error,
                        onRetry = { viewModel.loadData(apartment.id, apartment.transportation) }
                    )
                } else when (state.selectedSection) {
                    is ApartmentSection.Overview -> OverviewContent(apartment)
                    is ApartmentSection.Rooms -> RoomsContent(state.rooms)
                    is ApartmentSection.HouseRules -> HouseRulesContent(apartment)
                    is ApartmentSection.Checkout -> CheckoutContent(apartment, currentStay)
                    is ApartmentSection.Transport -> TransportContent(apartment, state.transportationServices)
                }
            }
        }
    }
}
