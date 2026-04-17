package com.touristapp.feature.apartment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touristapp.core.util.Resource
import com.touristapp.data.model.Room
import com.touristapp.data.model.TransportationItem
import com.touristapp.data.model.TransportationService
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
    val transportationServices: List<TransportationService> = emptyList(),
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

    fun loadData(apartmentId: String, transportationItems: List<TransportationItem>) {
        if (_uiState.value.rooms.isNotEmpty() || _uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            launch {
                when (val result = repository.getRooms(apartmentId)) {
                    is Resource.Success -> _uiState.update { it.copy(rooms = result.data) }
                    is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                    is Resource.Loading -> {}
                }
            }
            launch {
                val privateIds = transportationItems
                    .filter { it.type == "private" && it.transportationId.isNotBlank() }
                    .map { it.transportationId }
                when (val result = repository.getTransportationServices(privateIds)) {
                    is Resource.Success -> _uiState.update { it.copy(transportationServices = result.data) }
                    is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                    is Resource.Loading -> {}
                }
            }
        }.invokeOnCompletion {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectSection(section: ApartmentSection) {
        _uiState.update { it.copy(selectedSection = section) }
    }
}
