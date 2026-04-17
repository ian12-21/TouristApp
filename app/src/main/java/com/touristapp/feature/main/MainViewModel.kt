package com.touristapp.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touristapp.core.util.Resource
import com.touristapp.data.local.AppPreferences
import com.touristapp.data.model.Apartment
import com.touristapp.data.model.Guest
import com.touristapp.data.model.Place
import com.touristapp.data.model.Stay
import com.touristapp.data.model.WeatherInfo
import com.touristapp.domain.repository.TouristRepository
import com.touristapp.domain.repository.WeatherRepository
import com.touristapp.feature.places.PlaceCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OverlayScreen {
    data object None : OverlayScreen
    data object Apartment : OverlayScreen
    data class CategoryListing(val category: PlaceCategory) : OverlayScreen
    data class PlaceDetail(val place: Place) : OverlayScreen
}

data class MainUiState(
    val apartmentId: String? = null,
    val apartmentName: String = "",
    val apartment: Apartment? = null,
    val currentStay: Stay? = null,
    val guests: List<Guest> = emptyList(),
    val weatherInfo: WeatherInfo? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val overlayScreen: OverlayScreen = OverlayScreen.None,
    val cachedPlaces: List<Place> = emptyList()
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val touristRepository: TouristRepository,
    private val weatherRepository: WeatherRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        val savedId = prefs.getApartmentId()
        val savedName = prefs.getApartmentName() ?: ""
        _uiState.update { it.copy(apartmentId = savedId, apartmentName = savedName, isLoading = savedId != null) }
        if (savedId != null) {
            loadApartmentData(savedId)
        }
    }

    fun selectApartment(id: String) {
        prefs.setApartmentId(id)
        _uiState.update { it.copy(apartmentId = id, isLoading = true, error = null) }
        loadApartmentData(id)
    }

    fun retryLoad() {
        val id = _uiState.value.apartmentId ?: return
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadApartmentData(id)
    }

    fun reconfigure() {
        prefs.clear()
        _uiState.value = MainUiState()
    }

    fun navigateToApartment() {
        _uiState.update { it.copy(overlayScreen = OverlayScreen.Apartment) }
    }

    fun navigateToPlace(place: Place) {
        _uiState.update { it.copy(overlayScreen = OverlayScreen.PlaceDetail(place)) }
    }

    fun navigateToCategory(category: PlaceCategory) {
        _uiState.update { it.copy(overlayScreen = OverlayScreen.CategoryListing(category)) }
    }

    fun navigateBack() {
        _uiState.update { it.copy(overlayScreen = OverlayScreen.None) }
    }

    fun onPlacesLoaded(places: List<Place>) {
        _uiState.update { it.copy(cachedPlaces = places) }
    }

    private fun loadApartmentData(apartmentId: String) {
        viewModelScope.launch {
            when (val result = touristRepository.getApartment(apartmentId)) {
                is Resource.Success -> {
                    val apartment = result.data
                    prefs.setApartmentName(apartment.name)
                    _uiState.update {
                        it.copy(
                            apartment = apartment,
                            apartmentName = apartment.name,
                            isLoading = false
                        )
                    }
                    loadStayAndGuests(apartment)
                    startWeatherRefresh(apartment)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun loadStayAndGuests(apartment: Apartment) {
        val stayId = apartment.currentStayId ?: return
        viewModelScope.launch {
            when (val stayResult = touristRepository.getCurrentStay(stayId)) {
                is Resource.Success -> {
                    val stay = stayResult.data
                    _uiState.update { it.copy(currentStay = stay) }
                    if (stay.guestIds.isNotEmpty()) {
                        when (val guestsResult = touristRepository.getGuests(stay.guestIds)) {
                            is Resource.Success -> _uiState.update { it.copy(guests = guestsResult.data) }
                            else -> {}
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private fun startWeatherRefresh(apartment: Apartment) {
        val lat = apartment.coordinates["lat"] ?: return
        val lon = apartment.coordinates["lng"] ?: return
        viewModelScope.launch {
            while (true) {
                when (val result = weatherRepository.getCurrentWeather(lat, lon)) {
                    is Resource.Success -> _uiState.update { it.copy(weatherInfo = result.data) }
                    else -> {}
                }
                delay(WEATHER_REFRESH_INTERVAL_MS)
            }
        }
    }

    companion object {
        private const val WEATHER_REFRESH_INTERVAL_MS = 30 * 60 * 1000L
    }
}
