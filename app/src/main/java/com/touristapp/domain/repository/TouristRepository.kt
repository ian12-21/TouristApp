package com.touristapp.domain.repository

import com.touristapp.core.util.Resource
import com.touristapp.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TouristRepository {
    /**
     * Bumped on every pairing, even to the same apartment again. Screens drop what they
     * loaded for the previous pairing when it changes, so a re-pair reloads everything.
     */
    val pairingVersion: StateFlow<Int>
    /** Saves the owner and apartment that every later read is built from. */
    fun setPairing(pairing: Pairing)
    suspend fun ensureAnonymousAuth(): Resource<Unit>
    /** Uid of the tablet's guest session; the rules only let it edit reviews it wrote. */
    fun currentUid(): String?
    suspend fun getApartment(apartmentId: String, forceServer: Boolean = false): Resource<Apartment>
    /** Live state of the apartment document: emits on check-in, check-out and deletion. */
    fun observeApartmentStatus(apartmentId: String): Flow<ApartmentStatus>
    suspend fun getTransportationServices(serviceIds: List<String>, forceServer: Boolean = false): Resource<List<TransportationService>>
    /** Live stay document; null while it can't be read (not cached yet, or the listener is retrying). */
    fun observeStay(stayId: String): Flow<Stay?>
    suspend fun getPlacesForApartment(apartmentId: String, forceServer: Boolean = false): Resource<List<Place>>
    /** Live active places of the apartment: follows places hidden, shown or edited in the admin. */
    fun observePlacesForApartment(apartmentId: String): Flow<List<Place>>
    suspend fun getEmergencyContacts(groupId: String?, forceServer: Boolean = false): Resource<List<Contact>>
    suspend fun getReviewsForApartment(apartmentId: String): Resource<List<Review>>
    suspend fun getReviewForGuestAndStay(guestId: String, stayId: String): Resource<Review?>
    suspend fun createReview(review: Review): Resource<ReviewWriteStatus>
    suspend fun updateReview(reviewId: String, review: Review): Resource<ReviewWriteStatus>
    suspend fun getRooms(apartmentId: String, forceServer: Boolean = false): Resource<List<Room>>
}
