package com.touristapp.domain.repository

import com.touristapp.core.util.Resource
import com.touristapp.data.model.*

interface TouristRepository {
    suspend fun ensureAnonymousAuth(): Resource<Unit>
    suspend fun getApartment(apartmentId: String, forceServer: Boolean = false): Resource<Apartment>
    suspend fun getTransportationServices(serviceIds: List<String>, forceServer: Boolean = false): Resource<List<TransportationService>>
    suspend fun getCurrentStay(stayId: String, forceServer: Boolean = false): Resource<Stay>
    suspend fun getPlacesForApartment(apartmentId: String, forceServer: Boolean = false): Resource<List<Place>>
    suspend fun getEmergencyContacts(groupId: String?, forceServer: Boolean = false): Resource<List<Contact>>
    suspend fun getAllApartments(forceServer: Boolean = false): Resource<List<Pair<String, String>>>
    suspend fun getReviewsForApartment(apartmentId: String): Resource<List<Review>>
    suspend fun getReviewForGuestAndStay(guestId: String, stayId: String): Resource<Review?>
    suspend fun createReview(review: Review): Resource<Unit>
    suspend fun updateReview(reviewId: String, review: Review): Resource<Unit>
    suspend fun getRooms(apartmentId: String, forceServer: Boolean = false): Resource<List<Room>>
}
