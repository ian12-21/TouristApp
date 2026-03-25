package com.touristapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.touristapp.data.local.AppPreferences
import com.touristapp.ui.navigation.AppNavigation
import com.touristapp.ui.screens.setup.SetupScreen
import com.touristapp.ui.theme.TouristAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = AppPreferences(this)

        setContent {
            TouristAppTheme {
                val apartmentId = remember { mutableStateOf(prefs.getApartmentId()) }

                if (apartmentId.value == null) {
                    // First launch — owner sets up which apartment this tablet displays
                    SetupScreen(
                        onApartmentSelected = { id ->
                            prefs.setApartmentId(id)
                            apartmentId.value = id
                        }
                    )
                } else {
                    // Normal launch — guest-facing kiosk mode
                    AppNavigation(
                        apartmentId = apartmentId.value!!,
                        onReconfigure = {
                            prefs.clear()
                            apartmentId.value = null
                        }
                    )
                }
            }
        }
    }
}
