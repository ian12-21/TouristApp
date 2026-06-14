package com.touristapp.feature.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.touristapp.R
import com.touristapp.core.ui.components.ErrorContent
import com.touristapp.core.ui.components.WeatherDialog
import com.touristapp.core.ui.components.weatherIconFor
import com.touristapp.data.model.Place
import com.touristapp.feature.admin.AdminDialog
import com.touristapp.feature.apartment.ApartmentScreen
import com.touristapp.feature.home.HomeSlide
import com.touristapp.feature.places.CategoryListingScreen
import com.touristapp.feature.places.PlaceCategory
import com.touristapp.feature.places.PlaceDetailScreen
import com.touristapp.feature.places.PlacesSlide
import com.touristapp.feature.reviews.ReviewsSlide
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    uiState: MainUiState,
    onReconfigure: () -> Unit,
    onNavigateToApartment: () -> Unit,
    onNavigateToHouseRules: () -> Unit,
    onNavigateToPlace: (Place) -> Unit,
    onNavigateToCategory: (PlaceCategory) -> Unit,
    onNavigateBack: () -> Unit,
    onPlacesLoaded: (List<Place>) -> Unit,
    onRetryLoad: () -> Unit,
    onToggleTheme: () -> Unit,
    isKioskEnabled: Boolean,
    onExitKiosk: () -> Unit,
    onEnableKiosk: () -> Unit
) {
    if (uiState.apartment == null && uiState.error != null) {
        ErrorContent(message = uiState.error, onRetry = onRetryLoad)
        return
    }

    // In kiosk mode the system Back button must never let the guest leave the app.
    // An open overlay still closes on Back; at the root, Back is consumed (no-op).
    BackHandler(enabled = isKioskEnabled) {
        if (uiState.overlayScreen != OverlayScreen.None) {
            onNavigateBack()
        }
    }

    var showAdminDialog by remember { mutableStateOf(false) }
    var showWeatherDialog by remember { mutableStateOf(false) }
    var cooldownUntil by remember { mutableLongStateOf(0L) }

    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    when (val overlay = uiState.overlayScreen) {
        is OverlayScreen.Apartment -> {
            ApartmentScreen(
                apartment = uiState.apartment,
                apartmentName = uiState.apartmentName,
                currentStay = uiState.currentStay,
                initialSection = overlay.initialSection,
                onBack = onNavigateBack
            )
            return
        }
        is OverlayScreen.PlaceDetail -> {
            PlaceDetailScreen(
                place = overlay.place,
                apartmentId = uiState.apartmentId.orEmpty(),
                onBack = onNavigateBack
            )
            return
        }
        is OverlayScreen.CategoryListing -> {
            CategoryListingScreen(
                category = overlay.category,
                places = uiState.cachedPlaces,
                apartmentId = uiState.apartmentId.orEmpty(),
                onPlaceClick = onNavigateToPlace,
                onBack = onNavigateBack
            )
            return
        }
        OverlayScreen.None -> Unit
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    title = {
                        Text(
                            text = uiState.apartmentName,
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
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(0)
                                            }
                                        },
                                        onPress = {
                                            val pressStartTime = System.currentTimeMillis()
                                            val released = tryAwaitRelease()
                                            val pressDuration = System.currentTimeMillis() - pressStartTime
                                            if (released && pressDuration >= 5_000) {
                                                if (System.currentTimeMillis() > cooldownUntil) {
                                                    showAdminDialog = true
                                                }
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = stringResource(R.string.cd_home)
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(40.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                                .clickable { onToggleTheme() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = stringResource(R.string.cd_toggle_theme)
                            )
                        }
                        val weatherInfo = uiState.weatherInfo
                        val tempText = weatherInfo?.let { "${it.tempCelsius.roundToInt()}°" } ?: "—"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .height(40.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                                .then(
                                    if (weatherInfo != null)
                                        Modifier.clickable { showWeatherDialog = true }
                                    else Modifier
                                )
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(text = tempText, style = MaterialTheme.typography.labelLarge)
                            if (weatherInfo != null) {
                                Icon(
                                    imageVector = weatherIconFor(weatherInfo.iconCode),
                                    contentDescription = weatherInfo.condition,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("\uD83C\uDDEC\uD83C\uDDE7", style = MaterialTheme.typography.titleMedium)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 2,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> HomeSlide(
                        apartment = uiState.apartment,
                        currentStay = uiState.currentStay,
                        cachedEmergencyContacts = uiState.cachedEmergencyContacts,
                        onNavigateToReviews = {
                            coroutineScope.launch { pagerState.animateScrollToPage(2) }
                        },
                        onNavigateToApartment = onNavigateToApartment,
                        onNavigateToHouseRules = onNavigateToHouseRules,
                        onNavigateToExplore = {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        },
                        onNavigateToCategory = onNavigateToCategory
                    )
                    1 -> PlacesSlide(
                        apartmentId = uiState.apartmentId.orEmpty(),
                        cachedPlaces = uiState.cachedPlaces,
                        onSeeAll = onNavigateToCategory,
                        onPlaceClick = onNavigateToPlace,
                        onPlacesLoaded = onPlacesLoaded
                    )
                    2 -> ReviewsSlide(
                        apartmentId = uiState.apartmentId.orEmpty(),
                        currentStay = uiState.currentStay,
                        guests = uiState.guests
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                        label = "indicatorWidth"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        animationSpec = spring(),
                        label = "indicatorColor"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(50))
                            .background(color)
                    )
                }
            }
        }
    }

    if (showAdminDialog) {
        AdminDialog(
            onApartmentSelected = {
                showAdminDialog = false
                onReconfigure()
            },
            onDismiss = { showAdminDialog = false },
            onLockout = {
                cooldownUntil = System.currentTimeMillis() + 60_000
                showAdminDialog = false
            },
            isKioskEnabled = isKioskEnabled,
            onExitKiosk = {
                showAdminDialog = false
                onExitKiosk()
            },
            onEnableKiosk = {
                showAdminDialog = false
                onEnableKiosk()
            }
        )
    }

    if (showWeatherDialog && uiState.weatherInfo != null) {
        WeatherDialog(
            weatherInfo = uiState.weatherInfo,
            weekForecast = uiState.weekForecast,
            onDismiss = { showWeatherDialog = false }
        )
    }
}
