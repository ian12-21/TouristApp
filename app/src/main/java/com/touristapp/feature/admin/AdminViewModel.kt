package com.touristapp.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touristapp.core.util.Resource
import com.touristapp.domain.repository.TouristRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AdminUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val failedAttempts: Int = 0,
    val apartments: List<Pair<String, String>>? = null,
    val isLockedOut: Boolean = false
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: TouristRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun signIn(onLockout: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(_uiState.value.email, _uiState.value.password).await()
                when (val result = repository.getAllApartments()) {
                    is Resource.Success -> _uiState.update {
                        it.copy(apartments = result.data, isLoading = false)
                    }
                    is Resource.Error -> throw Exception(result.message)
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                val newAttempts = _uiState.value.failedAttempts + 1
                if (newAttempts >= 3) {
                    auth.signOut()
                    _uiState.update { it.copy(isLockedOut = true, isLoading = false) }
                    onLockout()
                } else {
                    _uiState.update {
                        it.copy(
                            failedAttempts = newAttempts,
                            errorMessage = "Invalid credentials",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun selectApartment(id: String, onSelected: (String) -> Unit) {
        auth.signOut()
        onSelected(id)
    }
}
