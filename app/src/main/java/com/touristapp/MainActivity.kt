package com.touristapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.touristapp.data.local.AppPreferences
import com.touristapp.data.model.Apartment
import com.touristapp.data.model.Stay
import com.touristapp.data.repository.TouristRepository
import com.touristapp.ui.navigation.AppNavigation
import com.touristapp.ui.screens.setup.SetupScreen
import com.touristapp.ui.theme.TouristAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = AppPreferences(this)
        val repository = TouristRepository()

        setContent {
            TouristAppTheme {
                val apartmentId = remember { mutableStateOf(prefs.getApartmentId()) }
                val apartmentName = remember { mutableStateOf(prefs.getApartmentName() ?: "") }
                val apartment = remember { mutableStateOf<Apartment?>(null) }
                val currentStay = remember { mutableStateOf<Stay?>(null) }

                LaunchedEffect(apartmentId.value) {
                    apartmentId.value?.let { id ->
                        val fetchedApartment = repository.getApartment(id)
                        apartment.value = fetchedApartment
                        fetchedApartment?.name?.let { name ->
                            prefs.setApartmentName(name)
                            apartmentName.value = name
                        }
                        fetchedApartment?.currentStayId?.let { stayId ->
                            currentStay.value = repository.getCurrentStay(stayId)
                        }
                    }
                }

                if (apartmentId.value == null) {
                    SetupScreen(
                        onApartmentSelected = { id ->
                            prefs.setApartmentId(id)
                            apartmentId.value = id
                        }
                    )
                } else {
                    AppNavigation(
                        apartmentId = apartmentId.value!!,
                        apartmentName = apartmentName.value,
                        apartment = apartment.value,
                        currentStay = currentStay.value,
                        onReconfigure = {
                            prefs.clear()
                            apartmentId.value = null
                            apartmentName.value = ""
                            apartment.value = null
                            currentStay.value = null
                        }
                    )
                }
            }
        }
    }
}
