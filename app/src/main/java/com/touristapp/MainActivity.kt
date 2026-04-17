package com.touristapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touristapp.core.ui.theme.TouristAppTheme
import com.touristapp.feature.main.AppNavigation
import com.touristapp.feature.main.MainViewModel
import com.touristapp.feature.setup.SetupScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TouristAppTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                if (uiState.apartmentId == null) {
                    SetupScreen(
                        onApartmentSelected = viewModel::selectApartment
                    )
                } else {
                    AppNavigation(
                        uiState = uiState,
                        onReconfigure = viewModel::reconfigure,
                        onNavigateToApartment = viewModel::navigateToApartment,
                        onNavigateToPlace = viewModel::navigateToPlace,
                        onNavigateToCategory = viewModel::navigateToCategory,
                        onNavigateBack = viewModel::navigateBack,
                        onPlacesLoaded = viewModel::onPlacesLoaded,
                        onRetryLoad = viewModel::retryLoad
                    )
                }
            }
        }
    }
}
