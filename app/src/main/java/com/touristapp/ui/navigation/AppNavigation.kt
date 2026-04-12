package com.touristapp.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.touristapp.data.model.Apartment
import com.touristapp.data.model.Guest
import com.touristapp.data.model.Stay
import com.touristapp.data.repository.TouristRepository
// import com.touristapp.data.model.WeatherInfo
import com.touristapp.ui.components.AdminDialog
// import com.touristapp.ui.components.WeatherDialog
// import com.touristapp.ui.components.weatherIconFor
// import kotlin.math.roundToInt
import com.touristapp.ui.screens.apartment.ApartmentScreen
import com.touristapp.ui.screens.home.HomeSlide
import com.touristapp.ui.screens.places.PlacesSlide
import com.touristapp.ui.screens.reviews.ReviewsSlide
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    apartmentId: String,
    apartmentName: String,
    apartment: Apartment?,
    currentStay: Stay?,
    guests: List<Guest>,
    repository: TouristRepository,
    // weatherInfo: WeatherInfo?,
    onReconfigure: () -> Unit
) {
    var showAdminDialog by remember { mutableStateOf(false) }
    // var showWeatherDialog by remember { mutableStateOf(false) }
    var cooldownUntil by remember { mutableLongStateOf(0L) }
    var showApartmentScreen by remember { mutableStateOf(false) }

    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    if (showApartmentScreen) {
        ApartmentScreen(
            apartment = apartment,
            apartmentName = apartmentName,
            currentStay = currentStay,
            repository = repository,
            onBack = { showApartmentScreen = false }
        )
        return
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

                                        if (released && pressDuration >= 10_000) {
                                            if (System.currentTimeMillis() > cooldownUntil) {
                                                showAdminDialog = true
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .height(40.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = "25°",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "☀\uFE0F",
                            style = MaterialTheme.typography.titleMedium
                        )
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
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> HomeSlide(
                        apartment = apartment,
                        currentStay = currentStay,
                        repository = repository,
                        onNavigateToReviews = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        },
                        onNavigateToApartment = { showApartmentScreen = true }
                    )
                    1 -> PlacesSlide()
                    2 -> ReviewsSlide(
                        apartmentId = apartmentId,
                        currentStay = currentStay,
                        guests = guests,
                        repository = repository
                    )
                }
            }

            // Page indicator dots
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
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = 300f
                        ),
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

    // if (showWeatherDialog && weatherInfo != null) {
    //     WeatherDialog(
    //         weatherInfo = weatherInfo,
    //         onDismiss = { showWeatherDialog = false }
    //     )
    // }

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
