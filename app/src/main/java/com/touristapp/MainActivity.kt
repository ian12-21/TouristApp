package com.touristapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touristapp.core.ui.theme.TouristAppTheme
import com.touristapp.feature.main.AppNavigation
import com.touristapp.feature.main.MainViewModel
import com.touristapp.feature.setup.SetupScreen
import com.touristapp.kiosk.KioskManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val kioskManager = remember { KioskManager(this@MainActivity) }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                var skippedFirstResume = false
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        if (skippedFirstResume) {
                            viewModel.refresh()
                        } else {
                            skippedFirstResume = true
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            TouristAppTheme(darkTheme = uiState.isDarkTheme) {
                if (uiState.apartmentId == null) {
                    SetupScreen(
                        onApartmentSelected = viewModel::selectApartment
                    )
                } else {
                    LaunchedEffect(uiState.isKioskEnabled) {
                        if (kioskManager.isDeviceOwner() && uiState.isKioskEnabled) {
                            kioskManager.enterKioskMode()
                        }
                    }
                    AppNavigation(
                        uiState = uiState,
                        onReconfigure = viewModel::reconfigure,
                        onNavigateToApartment = viewModel::navigateToApartment,
                        onNavigateToPlace = viewModel::navigateToPlace,
                        onNavigateToCategory = viewModel::navigateToCategory,
                        onNavigateBack = viewModel::navigateBack,
                        onPlacesLoaded = viewModel::onPlacesLoaded,
                        onRetryLoad = viewModel::retryLoad,
                        onToggleTheme = viewModel::toggleTheme,
                        isKioskEnabled = uiState.isKioskEnabled,
                        onExitKiosk = {
                            viewModel.setKioskEnabled(false)
                            kioskManager.exitKioskMode()
                        },
                        onEnableKiosk = {
                            viewModel.setKioskEnabled(true)
                            kioskManager.enterKioskMode()
                        }
                    )
                }
            }
        }
    }
}
