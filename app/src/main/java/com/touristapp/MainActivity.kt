package com.touristapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import java.util.Locale
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
import androidx.lifecycle.repeatOnLifecycle
import com.touristapp.core.i18n.ProvideLocalizedContext
import com.touristapp.core.ui.components.LanguageSwitchOverlay
import com.touristapp.core.ui.theme.TouristAppTheme
import com.touristapp.feature.main.AppNavigation
import com.touristapp.feature.main.MainViewModel
import com.touristapp.feature.setup.SetupScreen
import com.touristapp.kiosk.KioskManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Apply the stored per-app language before the UI is created so every
    // stringResource() resolves in that language. Read SharedPreferences directly
    // because Hilt injection is not yet available at attachBaseContext.
    override fun attachBaseContext(newBase: Context) {
        val lang = newBase
            .getSharedPreferences("kiosk_prefs", Context.MODE_PRIVATE)
            .getString("language", "en") ?: "en"
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(Locale(lang))
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

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

            // Weather polling is driven from here rather than from viewModelScope, so it
            // only runs while the app is actually visible. repeatOnLifecycle(STARTED)
            // cancels the loop on ON_STOP and starts a fresh one on ON_START; keying the
            // LaunchedEffect on the coordinates means it also (re)starts when the
            // apartment finishes loading or the owner reconfigures the tablet.
            val coordinates = uiState.apartment?.coordinates
            val latitude = coordinates?.get("lat")
            val longitude = coordinates?.get("lng")
            LaunchedEffect(lifecycleOwner, latitude, longitude) {
                if (latitude == null || longitude == null) return@LaunchedEffect
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.runWeatherRefresh(latitude, longitude)
                }
            }
            // Apply the chosen language reactively (no Activity recreate, so the switch can be
            // animated). This re-resolves every stringResource() and the weather day-name locale
            // when uiState.language changes. attachBaseContext still seeds the locale on cold start.
            // Dialogs re-apply ProvideLocalizedContext themselves (separate windows don't inherit
            // the platform LocalContext override).
            ProvideLocalizedContext(language = uiState.language) {
                TouristAppTheme(darkTheme = uiState.isDarkTheme) {
                    LanguageSwitchOverlay(isSwitching = uiState.isSwitchingLanguage) {
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
                                currentLanguage = uiState.language,
                                onSelectLanguage = viewModel::setLanguage,
                                onApartmentSelected = viewModel::selectApartment,
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
    }
}
