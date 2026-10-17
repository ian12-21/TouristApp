package com.touristapp.feature.places

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touristapp.core.util.Resource
import com.touristapp.data.model.Place
import com.touristapp.domain.repository.TouristRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlacesUiState(
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val repository: TouristRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlacesUiState())
    val uiState: StateFlow<PlacesUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    /** The [TouristRepository.pairingVersion] that [uiState] was loaded for. */
    private var pairingVersion = repository.pairingVersion.value

    init {
        viewModelScope.launch {
            repository.pairingVersion.collect { dropPreviousPairing() }
        }
    }

    /**
     * A re-pair drops the previous apartment's places, so the guards below load them
     * again instead of keeping the old owner's on screen.
     *
     * Also called before every load, so it doesn't matter whether the screen's reload or
     * the [init] collector sees the new pairing first.
     */
    private fun dropPreviousPairing() {
        val version = repository.pairingVersion.value
        if (version == pairingVersion) return
        pairingVersion = version
        loadJob?.cancel()
        _uiState.value = PlacesUiState()
    }

    /**
     * Seeds prefetched places so the screen renders instantly; loadPlaces still refreshes.
     * Re-seeds whenever the incoming list differs — e.g. after a language change, when the
     * caller hands down a freshly re-localized list — so the screen never shows stale content.
     */
    fun seed(places: List<Place>) {
        dropPreviousPairing()
        if (places.isEmpty() || places == _uiState.value.places) return
        _uiState.update { it.copy(places = places, isLoading = false, error = null) }
    }

    fun loadPlaces(apartmentId: String) {
        dropPreviousPairing()
        if (_uiState.value.places.isNotEmpty()) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            when (val result = repository.getPlacesForApartment(apartmentId)) {
                is Resource.Success -> _uiState.update {
                    it.copy(places = result.data, isLoading = false)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}
