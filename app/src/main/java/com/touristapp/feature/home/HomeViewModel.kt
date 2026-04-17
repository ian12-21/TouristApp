package com.touristapp.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touristapp.core.util.Resource
import com.touristapp.data.model.Contact
import com.touristapp.domain.repository.TouristRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val emergencyContacts: List<Contact> = emptyList(),
    val isLoadingContacts: Boolean = false,
    val contactsError: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TouristRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadEmergencyContacts() {
        if (_uiState.value.emergencyContacts.isNotEmpty()) return
        _uiState.update { it.copy(isLoadingContacts = true, contactsError = null) }
        viewModelScope.launch {
            when (val result = repository.getEmergencyContactsCroatia()) {
                is Resource.Success -> _uiState.update {
                    it.copy(emergencyContacts = result.data, isLoadingContacts = false)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoadingContacts = false, contactsError = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}
