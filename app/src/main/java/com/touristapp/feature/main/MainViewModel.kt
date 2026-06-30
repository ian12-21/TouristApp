package com.touristapp.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touristapp.core.util.Resource
import com.touristapp.data.local.AppPreferences
import com.touristapp.data.model.Apartment
import com.touristapp.data.model.Contact
import com.touristapp.data.model.Guest
import com.touristapp.data.model.Place
import com.touristapp.data.model.Stay
import com.touristapp.data.model.DailyForecast
import com.touristapp.data.model.TransportationService
import com.touristapp.data.model.WeatherInfo
import com.touristapp.domain.repository.TouristRepository
import com.touristapp.domain.repository.WeatherRepository
import com.touristapp.feature.apartment.ApartmentSection
import com.touristapp.feature.places.PlaceCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OverlayScreen {
    data object None : OverlayScreen
    data class Apartment(val initialSection: ApartmentSection? = null) : OverlayScreen
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
    val weekForecast: List<DailyForecast> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val overlayScreen: OverlayScreen = OverlayScreen.None,
    val cachedPlaces: List<Place> = emptyList(),
    val cachedEmergencyContacts: List<Contact> = emptyList(),
    val transportationServices: List<TransportationService> = emptyList(),
    val isDarkTheme: Boolean = true,
    val isKioskEnabled: Boolean = false,
    val language: String = "en"
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val touristRepository: TouristRepository,
    private val weatherRepository: WeatherRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var weatherJob: Job? = null

    init {
        val savedId = prefs.getApartmentId()
        val savedName = prefs.getApartmentName() ?: ""
        _uiState.update {
            it.copy(
                apartmentId = savedId,
                apartmentName = savedName,
                isLoading = savedId != null,
                weatherInfo = prefs.getLastWeather(),
                isDarkTheme = prefs.isDarkTheme(),
                isKioskEnabled = prefs.isKioskEnabled(),
                language = prefs.getLanguage()
            )
        }
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

    /**
     * Silent background refresh from the server, called when the app returns to
     * the foreground. Bounds how stale cache-first screen reads can be.
     */
    fun refresh() {
        val id = _uiState.value.apartmentId ?: return
        loadApartmentData(id, forceServer = true)
    }

    fun reconfigure() {
        weatherJob?.cancel()
        weatherJob = null
        prefs.clear()
        _uiState.value = MainUiState(
            isDarkTheme = prefs.isDarkTheme(),
            isKioskEnabled = prefs.isKioskEnabled(),
            language = prefs.getLanguage()
        )
    }

    fun setKioskEnabled(enabled: Boolean) {
        prefs.setKioskEnabled(enabled)
        _uiState.update { it.copy(isKioskEnabled = enabled) }
    }

    fun toggleTheme() {
        val next = !_uiState.value.isDarkTheme
        prefs.setDarkTheme(next)
        _uiState.update { it.copy(isDarkTheme = next) }
    }

    /**
     * Persist the chosen language and re-resolve already-loaded content to it.
     * The Activity recreates afterwards to apply the per-app locale to UI chrome;
     * this re-read is cache-only (no network) since the raw maps are unchanged.
     */
    fun setLanguage(code: String) {
        if (code == _uiState.value.language) return
        prefs.setLanguage(code)
        _uiState.update { it.copy(language = code) }
        _uiState.value.apartmentId?.let { loadApartmentData(it) }
    }

    fun navigateToApartment() {
        _uiState.update { it.copy(overlayScreen = OverlayScreen.Apartment()) }
    }

    fun navigateToHouseRules() {
        _uiState.update { it.copy(overlayScreen = OverlayScreen.Apartment(ApartmentSection.HouseRules)) }
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

    private fun loadApartmentData(apartmentId: String, forceServer: Boolean = false) {
        viewModelScope.launch {
            when (val result = touristRepository.getApartment(apartmentId, forceServer)) {
                is Resource.Success -> {
                    val apartment = result.data
                    prefs.setApartmentName(apartment.name)
                    _uiState.update {
                        it.copy(
                            apartment = apartment,
                            apartmentName = apartment.name,
                            isLoading = false,
                            error = null
                        )
                    }
                    loadStayAndGuests(apartment, forceServer)
                    prefetchSecondaryData(apartmentId, apartment, forceServer)
                    startWeatherRefresh(apartment)
                }
                is Resource.Error -> {
                    // Keep any cached data already on screen during a silent refresh.
                    if (!forceServer) {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun loadStayAndGuests(apartment: Apartment, forceServer: Boolean) {
        val stayId = apartment.currentStayId ?: return
        viewModelScope.launch {
            when (val stayResult = touristRepository.getCurrentStay(stayId, forceServer)) {
                is Resource.Success -> {
                    val stay = stayResult.data
                    _uiState.update { it.copy(currentStay = stay) }
                    if (stay.guestIds.isNotEmpty()) {
                        when (val guestsResult = touristRepository.getGuests(stay.guestIds, forceServer)) {
                            is Resource.Success -> _uiState.update { it.copy(guests = guestsResult.data) }
                            else -> {}
                        }
                    }
                }
                else -> {}
            }
        }
    }

    /** Warms the caches for screens the user is likely to open next. */
    private fun prefetchSecondaryData(apartmentId: String, apartment: Apartment, forceServer: Boolean) {
        viewModelScope.launch {
            when (val result = touristRepository.getPlacesForApartment(apartmentId, forceServer)) {
                is Resource.Success -> _uiState.update { it.copy(cachedPlaces = result.data) }
                else -> {}
            }
        }
        viewModelScope.launch {
            when (val result = touristRepository.getEmergencyContactsCroatia(forceServer)) {
                is Resource.Success -> _uiState.update { it.copy(cachedEmergencyContacts = result.data) }
                else -> {}
            }
        }
        viewModelScope.launch {
            val privateIds = apartment.transportation
                .filter { it.type == "private" && it.transportationId.isNotBlank() }
                .map { it.transportationId }
            // Always fetch from server so thumbImageUrl is never stale from old cache
            when (val result = touristRepository.getTransportationServices(privateIds, forceServer = true)) {
                is Resource.Success -> _uiState.update { it.copy(transportationServices = result.data) }
                else -> {}
            }
        }
    }

    private fun startWeatherRefresh(apartment: Apartment) {
        val lat = apartment.coordinates["lat"] ?: return
        val lon = apartment.coordinates["lng"] ?: return
        if (weatherJob?.isActive == true) return
        weatherJob = viewModelScope.launch {
            while (true) {
                when (val result = weatherRepository.getCurrentWeather(lat, lon)) {
                    is Resource.Success -> {
                        prefs.setLastWeather(result.data)
                        _uiState.update { it.copy(weatherInfo = result.data) }
                    }
                    else -> {}
                }
                when (val result = weatherRepository.getWeekForecast(lat, lon)) {
                    is Resource.Success -> _uiState.update { it.copy(weekForecast = result.data) }
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
