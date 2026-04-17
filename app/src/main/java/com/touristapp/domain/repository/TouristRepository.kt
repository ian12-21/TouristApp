package com.touristapp.domain.repository

import com.touristapp.core.util.Resource
import com.touristapp.data.model.*

interface TouristRepository {
    suspend fun ensureAnonymousAuth(): Resource<Unit>
    suspend fun getApartment(apartmentId: String): Resource<Apartment>
    suspend fun getTransportationServices(serviceIds: List<String>): Resource<List<TransportationService>>
    suspend fun getCurrentStay(stayId: String): Resource<Stay>
    suspend fun getGuest(guestId: String): Resource<Guest>
    suspend fun getGuests(guestIds: List<String>): Resource<List<Guest>>
    suspend fun getPlacesForApartment(apartmentId: String): Resource<List<Place>>
    suspend fun getEmergencyContactsCroatia(): Resource<List<Contact>>
    suspend fun getAllApartments(): Resource<List<Pair<String, String>>>
    suspend fun getReviewsForApartment(apartmentId: String): Resource<List<Review>>
    suspend fun getReviewForGuestAndStay(guestId: String, stayId: String): Resource<Review?>
    suspend fun createReview(review: Review): Resource<Unit>
    suspend fun updateReview(reviewId: String, review: Review): Resource<Unit>
    suspend fun getRooms(apartmentId: String): Resource<List<Room>>
}
