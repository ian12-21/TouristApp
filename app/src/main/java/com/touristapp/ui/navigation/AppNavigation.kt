package com.touristapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.touristapp.ui.screens.home.HomeScreen
import com.touristapp.ui.screens.places.PlacesScreen
import com.touristapp.ui.screens.map.MapScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Places : Screen("places", "Explore", Icons.Default.Place)
    data object Map : Screen("map", "Map", Icons.Default.LocationOn)
}

val bottomNavScreens = listOf(Screen.Home, Screen.Places, Screen.Map)

@Composable
fun AppNavigation(apartmentId: String, onReconfigure: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    apartmentId = apartmentId,
                    onReconfigure = onReconfigure
                )
            }
            composable(Screen.Places.route) {
                PlacesScreen(apartmentId = apartmentId)
            }
            composable(Screen.Map.route) {
                MapScreen(apartmentId = apartmentId)
            }
        }
    }
}
