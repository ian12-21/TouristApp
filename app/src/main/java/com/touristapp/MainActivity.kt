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
import com.touristapp.data.model.WeatherInfo
import com.touristapp.data.repository.TouristRepository
import com.touristapp.data.repository.WeatherRepository
import kotlinx.coroutines.delay
import com.touristapp.ui.navigation.AppNavigation
import com.touristapp.ui.screens.setup.SetupScreen
import com.touristapp.ui.theme.TouristAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = AppPreferences(this)
        val repository = TouristRepository()
        val weatherRepository = WeatherRepository()

        setContent {
            TouristAppTheme {
                val apartmentId = remember { mutableStateOf(prefs.getApartmentId()) }
                val apartmentName = remember { mutableStateOf(prefs.getApartmentName() ?: "") }
                val apartment = remember { mutableStateOf<Apartment?>(null) }
                val currentStay = remember { mutableStateOf<Stay?>(null) }
                val weatherInfo = remember { mutableStateOf<WeatherInfo?>(null) }

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

                        // Fetch weather using apartment coordinates, refresh every 30 min
                        fetchedApartment?.coordinates?.let { coords ->
                            val lat = coords["lat"] ?: return@let
                            val lon = coords["lng"] ?: return@let
                            Log.d("DEBUG", "Fetching weather for ${fetchedApartment.coordinates}")
                            weatherInfo.value = weatherRepository.getCurrentWeather(lat, lon)
                            while (true) {
                                delay(30 * 60 * 1000L)
                                weatherInfo.value = weatherRepository.getCurrentWeather(lat, lon)
                            }
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
                        weatherInfo = weatherInfo.value,
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
