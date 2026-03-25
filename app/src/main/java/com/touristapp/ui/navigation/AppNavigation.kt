package com.touristapp.ui.navigation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.touristapp.ui.components.AdminDialog
import com.touristapp.ui.screens.home.HomeScreen
import com.touristapp.ui.screens.map.MapScreen
import com.touristapp.ui.screens.places.PlacesScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Places : Screen("places", "Explore", Icons.Default.Place)
    data object Map : Screen("map", "Map", Icons.Default.LocationOn)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(apartmentId: String, apartmentName: String, onReconfigure: () -> Unit) {
    val navController = rememberNavController()
    var showAdminDialog by remember { mutableStateOf(false) }
    var cooldownUntil by remember { mutableLongStateOf(0L) }

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
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
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
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(apartmentId = apartmentId)
            }
            composable(Screen.Places.route) {
                PlacesScreen(apartmentId = apartmentId)
            }
            composable(Screen.Map.route) {
                MapScreen(apartmentId = apartmentId)
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
