package com.touristapp.domain.repository

import com.touristapp.core.util.Resource
import com.touristapp.data.model.*
import kotlinx.coroutines.flow.Flow

interface TouristRepository {
    suspend fun ensureAnonymousAuth(): Resource<Unit>
    /** Uid of the tablet's guest session; the rules only let it edit reviews it wrote. */
    fun currentUid(): String?
    suspend fun getApartment(apartmentId: String, forceServer: Boolean = false): Resource<Apartment>
    /** Live state of the apartment document: emits on check-in, check-out and deletion. */
    fun observeApartmentStatus(apartmentId: String): Flow<ApartmentStatus>
    suspend fun getTransportationServices(serviceIds: List<String>, forceServer: Boolean = false): Resource<List<TransportationService>>
    suspend fun getCurrentStay(stayId: String, forceServer: Boolean = false): Resource<Stay>
    suspend fun getPlacesForApartment(apartmentId: String, forceServer: Boolean = false): Resource<List<Place>>
    suspend fun getEmergencyContacts(groupId: String?, forceServer: Boolean = false): Resource<List<Contact>>
    suspend fun getReviewsForApartment(apartmentId: String): Resource<List<Review>>
    suspend fun getReviewForGuestAndStay(guestId: String, stayId: String): Resource<Review?>
    suspend fun createReview(review: Review): Resource<ReviewWriteStatus>
    suspend fun updateReview(reviewId: String, review: Review): Resource<ReviewWriteStatus>
    suspend fun getRooms(apartmentId: String, forceServer: Boolean = false): Resource<List<Room>>
}
