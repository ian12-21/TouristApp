package com.touristapp.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touristapp.core.util.ErrorReason
import com.touristapp.core.util.Resource
import com.touristapp.data.model.ApartmentChoice
import com.touristapp.data.model.Pairing
import com.touristapp.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Why the last sign-in step failed; the UI shows the matching string resource. */
enum class AdminError { INVALID_CREDENTIALS, NO_NETWORK, NOT_ACTIVATED, LOAD_APARTMENTS_FAILED }

data class AdminUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: AdminError? = null,
    val failedAttempts: Int = 0,
    val apartments: List<ApartmentChoice>? = null,
    val isLockedOut: Boolean = false,
    val isAuthenticated: Boolean = false
)

/**
 * Drives the owner-only admin dialog.
 *
 * Every auth call here goes through [AdminRepository], which owns a Firebase
 * session separate from the guest-facing one. That separation is the whole point:
 * signing in or out must never disturb the tablet's anonymous identity, because
 * that identity is what lets the tablet edit the reviews it wrote.
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
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
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val signInResult = adminRepository.signIn(
                email = _uiState.value.email,
                password = _uiState.value.password
            )
            if (signInResult is Resource.Error) {
                if (signInResult.reason == ErrorReason.OFFLINE) {
                    // Not a wrong password, so it must not count toward the lockout.
                    _uiState.update { it.copy(error = AdminError.NO_NETWORK, isLoading = false) }
                } else {
                    registerFailedAttempt(onLockout)
                }
                return@launch
            }

            when (val apartments = adminRepository.getAllApartments()) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        apartments = apartments.data,
                        isAuthenticated = true,
                        isLoading = false,
                        failedAttempts = 0
                    )
                }
                // Credentials were fine, so this is a network or rules problem, not a
                // bad password. Counting it toward the lockout would punish the owner
                // for being offline.
                is Resource.Error -> {
                    adminRepository.signOut()
                    val error = when (apartments.reason) {
                        ErrorReason.OFFLINE -> AdminError.NO_NETWORK
                        // The rules let a password account list its own apartments
                        // only once owners/{uid} exists, so a denial here means the
                        // account was never activated (SCHEMA.md §3.8).
                        ErrorReason.PERMISSION_DENIED -> AdminError.NOT_ACTIVATED
                        else -> AdminError.LOAD_APARTMENTS_FAILED
                    }
                    _uiState.update { it.copy(error = error, isLoading = false) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun registerFailedAttempt(onLockout: () -> Unit) {
        val attempts = _uiState.value.failedAttempts + 1
        if (attempts >= MAX_ATTEMPTS) {
            adminRepository.signOut()
            _uiState.update { it.copy(isLockedOut = true, isLoading = false) }
            onLockout()
        } else {
            _uiState.update {
                it.copy(
                    failedAttempts = attempts,
                    error = AdminError.INVALID_CREDENTIALS,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Applies the owner's apartment choice and closes the admin session.
     *
     * Switching apartments is now purely a matter of which id the app stores — the
     * guest session is never re-authenticated, so the tablet keeps the same uid
     * across as many switches as the owner cares to make.
     */
    fun selectApartment(pairing: Pairing, onSelected: (Pairing) -> Unit) {
        // The owner id travels inside the pairing, so signing out first loses nothing.
        adminRepository.signOut()
        // Deliberately leaves isAuthenticated alone. AdminLoginFlow draws the login
        // form whenever that flag is false, so clearing it here would re-show the
        // form during the frame between this call and onSelected navigating away.
        // Clearing state is AdminDialog's job — it calls lock() on open, so the next
        // admin session starts from a blank form either way.
        onSelected(pairing)
    }

    fun lock() {
        adminRepository.signOut()
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                email = "",
                password = "",
                error = null,
                isLoading = false
            )
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
    }
}
