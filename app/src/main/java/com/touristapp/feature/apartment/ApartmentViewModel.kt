package com.touristapp.feature.apartment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touristapp.core.util.Resource
import com.touristapp.data.model.Room
import com.touristapp.domain.repository.TouristRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApartmentUiState(
    val rooms: List<Room> = emptyList(),
    val selectedSection: ApartmentSection = ApartmentSection.Overview,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ApartmentViewModel @Inject constructor(
    private val repository: TouristRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApartmentUiState())
    val uiState: StateFlow<ApartmentUiState> = _uiState.asStateFlow()

    /**
     * Re-fetches rooms every time the screen opens (the repository is server-first, so this picks
     * up admin edits and the current language). Only blocks while a load is already in flight.
     * Existing rooms stay on screen during the refresh; the spinner shows only on the first load.
     */
    fun loadData(apartmentId: String) {
        if (_uiState.value.isLoading) return
        if (_uiState.value.rooms.isEmpty()) {
            _uiState.update { it.copy(isLoading = true, error = null) }
        }
        viewModelScope.launch {
            when (val result = repository.getRooms(apartmentId)) {
                is Resource.Success -> _uiState.update { it.copy(rooms = result.data, error = null) }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }.invokeOnCompletion {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectSection(section: ApartmentSection) {
        _uiState.update { it.copy(selectedSection = section) }
    }
}
