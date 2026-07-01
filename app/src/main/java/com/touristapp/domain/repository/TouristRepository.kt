package com.touristapp.domain.repository

import com.touristapp.core.util.Resource
import com.touristapp.data.model.*
import kotlinx.coroutines.flow.Flow

interface TouristRepository {
    suspend fun ensureAnonymousAuth(): Resource<Unit>

    /**
     * Realtime listeners. Each emits immediately from the local cache (if present) and again on
     * every server change, so admin edits propagate to the tablet without a manual refresh.
     * Content is localized to the language selected at subscription time; re-subscribe to re-localize.
     */
    fun observeApartment(apartmentId: String): Flow<Resource<Apartment>>
    fun observeStay(stayId: String): Flow<Resource<Stay>>
    fun observeGuests(guestIds: List<String>): Flow<Resource<List<Guest>>>
    fun observePlacesForApartment(apartmentId: String): Flow<Resource<List<Place>>>

    suspend fun getApartment(apartmentId: String, forceServer: Boolean = false): Resource<Apartment>
    suspend fun getTransportationServices(serviceIds: List<String>, forceServer: Boolean = false): Resource<List<TransportationService>>
    suspend fun getCurrentStay(stayId: String, forceServer: Boolean = false): Resource<Stay>
    suspend fun getGuest(guestId: String, forceServer: Boolean = false): Resource<Guest>
    suspend fun getGuests(guestIds: List<String>, forceServer: Boolean = false): Resource<List<Guest>>
    suspend fun getPlacesForApartment(apartmentId: String, forceServer: Boolean = false): Resource<List<Place>>
    suspend fun getEmergencyContactsCroatia(forceServer: Boolean = false): Resource<List<Contact>>
    suspend fun getAllApartments(forceServer: Boolean = false): Resource<List<Pair<String, String>>>
    suspend fun getReviewsForApartment(apartmentId: String): Resource<List<Review>>
    suspend fun getReviewForGuestAndStay(guestId: String, stayId: String): Resource<Review?>
    suspend fun createReview(review: Review): Resource<Unit>
    suspend fun updateReview(reviewId: String, review: Review): Resource<Unit>
    suspend fun getRooms(apartmentId: String, forceServer: Boolean = false): Resource<List<Room>>
}
