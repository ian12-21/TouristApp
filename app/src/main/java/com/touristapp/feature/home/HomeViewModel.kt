package com.touristapp.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touristapp.core.util.Resource
import com.touristapp.data.model.Contact
import com.touristapp.domain.repository.TouristRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    /** Group [emergencyContacts] belongs to; differs after the tablet is re-paired. */
    val contactsGroupId: String? = null,
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

    private var loadJob: Job? = null
    /** The [TouristRepository.pairingVersion] that [uiState] was loaded for. */
    private var pairingVersion = repository.pairingVersion.value

    init {
        viewModelScope.launch {
            repository.pairingVersion.collect { dropPreviousPairing() }
        }
    }

    /**
     * A re-pair drops the previous apartment's contacts, so the guards below load them
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
        _uiState.value = HomeUiState()
    }

    /**
     * Seeds prefetched contacts so the dialog opens instantly; loadEmergencyContacts still refreshes.
     * Re-seeds when the incoming list differs so a language change (which re-localizes the contact
     * names upstream) propagates instead of being blocked by the already-loaded guard.
     */
    fun seed(groupId: String?, contacts: List<Contact>) {
        dropPreviousPairing()
        val state = _uiState.value
        if (contacts.isEmpty() || (groupId == state.contactsGroupId && contacts == state.emergencyContacts)) return
        _uiState.update {
            it.copy(contactsGroupId = groupId, emergencyContacts = contacts, isLoadingContacts = false)
        }
    }

    fun loadEmergencyContacts(groupId: String?) {
        dropPreviousPairing()
        val state = _uiState.value
        if (state.emergencyContacts.isNotEmpty() && state.contactsGroupId == groupId) return
        _uiState.update {
            it.copy(
                contactsGroupId = groupId,
                emergencyContacts = emptyList(),
                isLoadingContacts = true,
                contactsError = null
            )
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            when (val result = repository.getEmergencyContacts(groupId)) {
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
