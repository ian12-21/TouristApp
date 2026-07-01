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
import kotlinx.coroutines.flow.collect
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

    private var weatherJob: Job? = null

    // Realtime listeners. The apartment listener drives the stay/guests listeners as the apartment's
    // currentStayId/guestIds change; all are restarted on apartment or language switch.
    private var apartmentJob: Job? = null
    private var placesJob: Job? = null
    private var stayJob: Job? = null
    private var guestsJob: Job? = null
    private var observedStayId: String? = null
    private var observedGuestIds: List<String> = emptyList()

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
            observeApartmentData(savedId)
        }
    }

    fun selectApartment(id: String) {
        prefs.setApartmentId(id)
        _uiState.update { it.copy(apartmentId = id, isLoading = true, error = null) }
        observeApartmentData(id)
    }

    fun retryLoad() {
        val id = _uiState.value.apartmentId ?: return
        _uiState.update { it.copy(isLoading = true, error = null) }
        observeApartmentData(id)
    }

    /**
     * Foreground re-sync. Realtime listeners already keep content fresh while subscribed; this
     * re-subscribes so any change missed while listeners were torn down (backgrounded) is picked up.
     */
    fun refresh() {
        val id = _uiState.value.apartmentId ?: return
        observeApartmentData(id)
    }

    fun reconfigure() {
        cancelListeners()
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
     * Persist the chosen language and re-resolve already-loaded content to it. The locale is now
     * applied reactively in Compose (no Activity recreate), and a blur+spinner overlay covers the
     * reload: [isSwitchingLanguage] stays true until the apartment listener re-emits, so both the
     * localized strings and the re-resolved content swap in while hidden.
     */
    fun setLanguage(code: String) {
        if (code == _uiState.value.language) return
        prefs.setLanguage(code)
        val apartmentId = _uiState.value.apartmentId
        // Only show the reload overlay when there's content to re-resolve; otherwise it would never
        // clear (the apartment listener clears the flag on its next emission).
        _uiState.update { it.copy(language = code, isSwitchingLanguage = apartmentId != null) }
        // Re-subscribing re-runs the localized mappers against the (cached) documents, so all content
        // swaps to the new language while the blur overlay hides the transition.
        apartmentId?.let { observeApartmentData(it) }
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

    /**
     * Subscribes to realtime listeners for the apartment and its places. Each fresh call cancels the
     * previous subscriptions and starts over, which is also how a language switch re-localizes: the
     * listeners re-emit the cached documents through the now-current-language mappers.
     */
    private fun observeApartmentData(apartmentId: String) {
        cancelListeners()
        observedStayId = null
        observedGuestIds = emptyList()

        apartmentJob = viewModelScope.launch {
            touristRepository.observeApartment(apartmentId).collect { result ->
                when (result) {
                    is Resource.Success -> onApartmentUpdate(apartmentId, result.data)
                    is Resource.Error -> _uiState.update {
                        // Keep any cached content already on screen; only surface an error if there's
                        // nothing to show yet.
                        it.copy(
                            isLoading = false,
                            isSwitchingLanguage = false,
                            error = if (it.apartment == null) result.message else it.error
                        )
                    }
                    is Resource.Loading -> {}
                }
            }
        }

        placesJob = viewModelScope.launch {
            touristRepository.observePlacesForApartment(apartmentId).collect { result ->
                if (result is Resource.Success) updateCachedPlaces(result.data)
            }
        }
    }

    private fun onApartmentUpdate(apartmentId: String, apartment: Apartment) {
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
        observeStayAndGuests(apartment)
        loadSecondaryData(apartment)
        startWeatherRefresh(apartment)
    }

    /**
     * (Re)subscribes the stay listener when the apartment's [Apartment.currentStayId] changes, and
     * the guests listener when the stay's guest list changes. Guards against the apartment listener's
     * frequent re-emissions restarting these on every tick.
     */
    private fun observeStayAndGuests(apartment: Apartment) {
        val stayId = apartment.currentStayId
        if (stayId == observedStayId) return
        observedStayId = stayId
        observedGuestIds = emptyList()
        stayJob?.cancel()
        guestsJob?.cancel()

        if (stayId == null) {
            _uiState.update { it.copy(currentStay = null, guests = emptyList()) }
            return
        }
        stayJob = viewModelScope.launch {
            touristRepository.observeStay(stayId).collect { result ->
                if (result is Resource.Success) {
                    val stay = result.data
                    _uiState.update { it.copy(currentStay = stay) }
                    observeGuests(stay.guestIds)
                }
            }
        }
    }

    private fun observeGuests(guestIds: List<String>) {
        if (guestIds == observedGuestIds) return
        observedGuestIds = guestIds
        guestsJob?.cancel()
        if (guestIds.isEmpty()) {
            _uiState.update { it.copy(guests = emptyList()) }
            return
        }
        guestsJob = viewModelScope.launch {
            touristRepository.observeGuests(guestIds).collect { result ->
                if (result is Resource.Success) _uiState.update { it.copy(guests = result.data) }
            }
        }
    }

    /**
     * Loads content that changes rarely and isn't worth a standing listener: emergency contacts and
     * the apartment's private transportation services. Re-run on each apartment emission so a
     * language switch re-localizes them too.
     */
    private fun loadSecondaryData(apartment: Apartment) {
        viewModelScope.launch {
            when (val result = touristRepository.getEmergencyContactsCroatia()) {
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

    private fun cancelListeners() {
        apartmentJob?.cancel()
        placesJob?.cancel()
        stayJob?.cancel()
        guestsJob?.cancel()
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
