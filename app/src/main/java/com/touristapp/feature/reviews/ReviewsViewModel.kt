package com.touristapp.feature.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touristapp.core.util.Resource
import com.touristapp.core.util.dataOrEmpty
import com.touristapp.core.util.dataOrNull
import com.touristapp.data.model.Guest
import com.touristapp.data.model.Review
import com.touristapp.domain.repository.TouristRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewsUiState(
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showCreateSheet: Boolean = false,
    val editingReview: Review? = null,
    val selectedGuest: Guest? = null,
    val selectedGuestForEdit: Guest? = null,
    val isCheckingExisting: Boolean = false,
    val foundExistingReview: Review? = null,
    val cleanliness: Int = 5,
    val location: Int = 5,
    val comfort: Int = 5,
    val valueForMoney: Int = 5,
    val facilities: Int = 5,
    val communication: Int = 5,
    val wifi: Int = 5,
    val comment: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null
) {
    val overallScore: Double
        get() = listOf(cleanliness, location, comfort, valueForMoney, facilities, communication, wifi)
            .average()
            .let { Math.round(it * 10) / 10.0 }

    val isEditMode: Boolean get() = editingReview != null
}

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val repository: TouristRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    fun loadReviews(apartmentId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.getReviewsForApartment(apartmentId)) {
                is Resource.Success -> _uiState.update {
                    it.copy(reviews = result.data, isLoading = false)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun openCreateSheet() {
        _uiState.update {
            it.copy(
                showCreateSheet = true,
                editingReview = null,
                selectedGuestForEdit = null,
                selectedGuest = null,
                foundExistingReview = null,
                cleanliness = 5, location = 5, comfort = 5,
                valueForMoney = 5, facilities = 5, communication = 5, wifi = 5,
                comment = "", saveError = null
            )
        }
    }

    fun openEditSheet(review: Review, guest: Guest?) {
        _uiState.update {
            it.copy(
                showCreateSheet = true,
                editingReview = review,
                selectedGuestForEdit = guest,
                selectedGuest = guest,
                foundExistingReview = review,
                cleanliness = review.cleanliness,
                location = review.location,
                comfort = review.comfort,
                valueForMoney = review.valueForMoney,
                facilities = review.facilities,
                communication = review.communication,
                wifi = review.wifi,
                comment = review.comment,
                saveError = null
            )
        }
    }

    fun dismissCreateSheet() {
        _uiState.update {
            it.copy(
                showCreateSheet = false,
                editingReview = null,
                selectedGuestForEdit = null,
                selectedGuest = null,
                foundExistingReview = null,
                saveError = null
            )
        }
    }

    fun selectGuest(guest: Guest, stayId: String?) {
        _uiState.update { it.copy(selectedGuest = guest) }
        if (_uiState.value.isEditMode || stayId == null) return

        _uiState.update { it.copy(isCheckingExisting = true) }
        viewModelScope.launch {
            val existing = repository.getReviewForGuestAndStay(guest.id, stayId).dataOrNull()
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        foundExistingReview = existing,
                        cleanliness = existing.cleanliness,
                        location = existing.location,
                        comfort = existing.comfort,
                        valueForMoney = existing.valueForMoney,
                        facilities = existing.facilities,
                        communication = existing.communication,
                        wifi = existing.wifi,
                        comment = existing.comment,
                        isCheckingExisting = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        foundExistingReview = null,
                        cleanliness = 5, location = 5, comfort = 5,
                        valueForMoney = 5, facilities = 5, communication = 5, wifi = 5,
                        comment = "",
                        isCheckingExisting = false
                    )
                }
            }
        }
    }

    fun updateRating(category: String, value: Int) {
        _uiState.update {
            when (category) {
                "cleanliness" -> it.copy(cleanliness = value)
                "location" -> it.copy(location = value)
                "comfort" -> it.copy(comfort = value)
                "valueForMoney" -> it.copy(valueForMoney = value)
                "facilities" -> it.copy(facilities = value)
                "communication" -> it.copy(communication = value)
                "wifi" -> it.copy(wifi = value)
                else -> it
            }
        }
    }

    fun updateComment(comment: String) {
        if (comment.length <= 500) {
            _uiState.update { it.copy(comment = comment) }
        }
    }

    fun submitReview(apartmentId: String, stayId: String) {
        val state = _uiState.value
        val guest = state.selectedGuest ?: return

        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            try {
                repository.ensureAnonymousAuth()
                val review = Review(
                    apartmentId = apartmentId,
                    stayId = stayId,
                    guestId = guest.id,
                    guestName = guest.name,
                    cleanliness = state.cleanliness,
                    location = state.location,
                    comfort = state.comfort,
                    valueForMoney = state.valueForMoney,
                    facilities = state.facilities,
                    communication = state.communication,
                    wifi = state.wifi,
                    overallScore = state.overallScore,
                    comment = state.comment.trim()
                )

                val existingId = state.foundExistingReview?.id ?: state.editingReview?.id
                val result = if (existingId != null) {
                    repository.updateReview(existingId, review)
                } else {
                    repository.createReview(review)
                }

                _uiState.update { it.copy(isSaving = false) }
                if (result is Resource.Success) {
                    dismissCreateSheet()
                    loadReviews(apartmentId)
                } else if (result is Resource.Error) {
                    _uiState.update { it.copy(saveError = result.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message) }
            }
        }
    }
}
