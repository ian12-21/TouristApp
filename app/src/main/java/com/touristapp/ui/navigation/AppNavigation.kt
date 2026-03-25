package com.touristapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.touristapp.data.model.Apartment
import com.touristapp.data.model.Stay
import com.touristapp.ui.components.AdminDialog
import com.touristapp.ui.screens.home.HomeSlide
import com.touristapp.ui.screens.map.MapSlide
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
    onReconfigure: () -> Unit
) {
    var showAdminDialog by remember { mutableStateOf(false) }
    var cooldownUntil by remember { mutableLongStateOf(0L) }

    val pageCount = 4
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = apartmentName,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
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
                    IconButton(onClick = { /* future: language picker */ }) {
                        Text("\uD83C\uDDEC\uD83C\uDDE7", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
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
                    0 -> HomeSlide(apartment = apartment, currentStay = currentStay)
                    1 -> MapSlide()
                    2 -> PlacesSlide()
                    3 -> ReviewsSlide()
                }
            }

            // Page indicator dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }

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
