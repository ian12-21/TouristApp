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
    val language: String = "en",
    val isSwitchingLanguage: Boolean = false
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
        _uiState.update {
            it.copy(
                apartmentId = savedId,
                apartmentName = savedName,
                isLoading = savedId != null,
                weatherInfo = prefs.getLastWeather(),
                weekForecast = prefs.getLastForecast() ?: emptyList(),
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
     * Persist the chosen language and re-resolve already-loaded content to it. The locale is now
     * applied reactively in Compose (no Activity recreate), and a blur+spinner overlay covers the
     * reload: [isSwitchingLanguage] stays true until [loadApartmentData] completes, so both the
     * localized strings and the re-fetched content swap in while hidden.
     */
    fun setLanguage(code: String) {
        if (code == _uiState.value.language) return
        prefs.setLanguage(code)
        val apartmentId = _uiState.value.apartmentId
        // Only show the reload overlay when there's content to re-fetch; otherwise it would never
        // clear (loadApartmentData clears the flag on completion).
        _uiState.update { it.copy(language = code, isSwitchingLanguage = apartmentId != null) }
        apartmentId?.let { loadApartmentData(it) }
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
        updateCachedPlaces(places)
    }

    /**
     * Replaces the cached places and keeps any open place-detail overlay in sync: the overlay
     * holds a snapshot taken at navigation time, so without this a language change would leave
     * the open detail screen showing the previous language's description and tips.
     */
    private fun updateCachedPlaces(places: List<Place>) {
        _uiState.update { state ->
            val overlay = state.overlayScreen
            val refreshedOverlay = if (overlay is OverlayScreen.PlaceDetail) {
                places.firstOrNull { it.id == overlay.place.id }
                    ?.let { OverlayScreen.PlaceDetail(it) }
                    ?: overlay
            } else {
                overlay
            }
            state.copy(cachedPlaces = places, overlayScreen = refreshedOverlay)
        }
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
                            isSwitchingLanguage = false,
                            error = null
                        )
                    }
                    loadStayAndGuests(apartment, forceServer)
                    prefetchSecondaryData(apartmentId, apartment, forceServer)
                }
                is Resource.Error -> {
                    // Keep any cached data already on screen during a silent refresh.
                    if (!forceServer) {
                        _uiState.update {
                            it.copy(isLoading = false, isSwitchingLanguage = false, error = result.message)
                        }
                    } else {
                        // A failed language-switch refresh must still drop the overlay.
                        _uiState.update { it.copy(isSwitchingLanguage = false) }
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
                    // Guest names are denormalized onto the stay, so the tablet no longer
                    // reads the private `guests` collection. Preserve guestIds order.
                    val guests = stay.guestIds.map { id ->
                        Guest(id = id, name = stay.guestNames[id] ?: "")
                    }
                    _uiState.update { it.copy(currentStay = stay, guests = guests) }
                }
                else -> {}
            }
        }
    }

    /** Warms the caches for screens the user is likely to open next. */
    private fun prefetchSecondaryData(apartmentId: String, apartment: Apartment, forceServer: Boolean) {
        viewModelScope.launch {
            when (val result = touristRepository.getPlacesForApartment(apartmentId, forceServer)) {
                is Resource.Success -> updateCachedPlaces(result.data)
                else -> {}
            }
        }
        viewModelScope.launch {
            when (val result = touristRepository.getEmergencyContacts(apartment.emergencyContactGroupId, forceServer)) {
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

    suspend fun runWeatherRefresh(lat: Double, lon: Double) {
        while (true) {
            fetchWeather(lat, lon)
            delay(WEATHER_REFRESH_INTERVAL_MS)
        }
    }

    private suspend fun fetchWeather(lat: Double, lon: Double) {
        when (val result = weatherRepository.getCurrentWeather(lat, lon)) {
            is Resource.Success -> _uiState.update { it.copy(weatherInfo = result.data) }
            else -> {}
        }
        when (val result = weatherRepository.getWeekForecast(lat, lon)) {
            is Resource.Success -> _uiState.update { it.copy(weekForecast = result.data) }
            else -> {}
        }
    }

    companion object {
        /**
         * The loop fetches once immediately, then every 6 hours — so a cold start always
         * refreshes and a running tablet makes 4 calls a day.
         */
        private const val WEATHER_REFRESH_INTERVAL_MS = 6L * 60 * 60 * 1000
    }
}
