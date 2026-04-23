package com.touristapp.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.touristapp.core.util.Resource
import com.touristapp.data.model.*
import com.touristapp.domain.repository.TouristRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TouristRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TouristRepository {

    override suspend fun ensureAnonymousAuth(): Resource<Unit> {
        if (auth.currentUser == null) {
            return try {
                auth.signInAnonymously().await()
                Resource.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Anonymous auth failed", e)
                Resource.Error("Authentication failed", e)
            }
        }
        return Resource.Success(Unit)
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun getApartment(apartmentId: String): Resource<Apartment> {
        return try {
            val doc = db.collection("apartments").document(apartmentId).get().await()
            val apartment = doc.toObject(Apartment::class.java)?.copy(id = doc.id)
                ?: return Resource.Error("Apartment not found")

            val rawTransport = doc.get("transportation") as? List<Map<String, Any>> ?: emptyList()
            val transportItems = rawTransport.map { map ->
                TransportationItem(
                    type = map["type"] as? String ?: "",
                    description = map["description"] as? String ?: "",
                    transportationId = map["transportation_id"] as? String ?: ""
                )
            }
            Resource.Success(apartment.copy(transportation = transportItems))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching apartment $apartmentId", e)
            Resource.Error("Failed to load apartment", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun getTransportationServices(serviceIds: List<String>): Resource<List<TransportationService>> {
        if (serviceIds.isEmpty()) return Resource.Success(emptyList())
        return try {
            val services = serviceIds.chunked(30).flatMap { chunk ->
                chunk.map { id ->
                    db.collection("transportation").document(id)
                }.let { refs ->
                    db.collection("transportation")
                        .whereIn("__name__", refs)
                        .get()
                        .await()
                        .documents
                        .mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            val entry = data.entries.firstOrNull() ?: return@mapNotNull null
                            val name = entry.key
                            val details = entry.value as? Map<*, *> ?: return@mapNotNull null
                            TransportationService(
                                id = doc.id,
                                name = name,
                                phone = details["phone"] as? String ?: "",
                                description = details["description"] as? String ?: ""
                            )
                        }
                }
            }
            Resource.Success(services)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching transportation services", e)
            Resource.Error("Failed to load transportation services", e)
        }
    }

    override suspend fun getCurrentStay(stayId: String): Resource<Stay> {
        return try {
            val doc = db.collection("stays").document(stayId).get().await()
            val stay = doc.toObject(Stay::class.java)?.copy(id = doc.id)
                ?: return Resource.Error("Stay not found")
            Resource.Success(stay)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching stay $stayId", e)
            Resource.Error("Failed to load stay", e)
        }
    }

    override suspend fun getGuest(guestId: String): Resource<Guest> {
        return try {
            val doc = db.collection("guests").document(guestId).get().await()
            val guest = doc.toObject(Guest::class.java)?.copy(id = doc.id)
                ?: return Resource.Error("Guest not found")
            Resource.Success(guest)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching guest $guestId", e)
            Resource.Error("Failed to load guest", e)
        }
    }

    override suspend fun getGuests(guestIds: List<String>): Resource<List<Guest>> {
        if (guestIds.isEmpty()) return Resource.Success(emptyList())
        return try {
            val guests = guestIds.chunked(30).flatMap { chunk ->
                db.collection("guests")
                    .whereIn("__name__", chunk.map { db.collection("guests").document(it) })
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(Guest::class.java)?.copy(id = doc.id)
                    }
            }
            Resource.Success(guests)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching guests", e)
            Resource.Error("Failed to load guests", e)
        }
    }

    override suspend fun getPlacesForApartment(apartmentId: String): Resource<List<Place>> {
        return try {
            val snapshot = db.collection("places")
                .whereArrayContains("apartmentIds", apartmentId)
                .get()
                .await()
            val places = snapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(Place::class.java)?.copy(id = doc.id)
                }
                .filter { it.isActive }
            Resource.Success(places)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching places for apartment $apartmentId", e)
            Resource.Error("Failed to load places", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun getEmergencyContactsCroatia(): Resource<List<Contact>> {
        return try {
            val contacts = db.collection("emergency_contacts_croatia")
                .get()
                .await()
                .documents
                .flatMap { doc ->
                    val contactsList = doc.get("contacts") as? List<Map<String, String>> ?: emptyList()
                    contactsList.map { map ->
                        Contact(
                            name = map["name"] ?: "",
                            phone = map["phone"] ?: map["number"] ?: ""
                        )
                    }
                }
            Resource.Success(contacts)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching emergency contacts", e)
            Resource.Error("Failed to load emergency contacts", e)
        }
    }

    override suspend fun getAllApartments(): Resource<List<Pair<String, String>>> {
        return try {
            val apartments = db.collection("apartments")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val address = doc.getString("address") ?: ""
                    doc.id to "$name — $address"
                }
            Resource.Success(apartments)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching apartments", e)
            Resource.Error("Failed to load apartments", e)
        }
    }

    override suspend fun getReviewsForApartment(apartmentId: String): Resource<List<Review>> {
        return try {
            val reviews = db.collection("reviews")
                .whereEqualTo("apartmentId", apartmentId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(Review::class.java)?.copy(id = doc.id)
                }
            Resource.Success(reviews)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching reviews for apartment $apartmentId", e)
            Resource.Error("Failed to load reviews", e)
        }
    }

    override suspend fun getReviewForGuestAndStay(guestId: String, stayId: String): Resource<Review?> {
        return try {
            val docs = db.collection("reviews")
                .whereEqualTo("guestId", guestId)
                .whereEqualTo("stayId", stayId)
                .get()
                .await()
                .documents
            val review = docs.firstOrNull()?.let { doc ->
                doc.toObject(Review::class.java)?.copy(id = doc.id)
            }
            Resource.Success(review)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching review for guest $guestId, stay $stayId", e)
            Resource.Error("Failed to check existing review", e)
        }
    }

    override suspend fun createReview(review: Review): Resource<Unit> {
        return try {
            val data = hashMapOf(
                "apartmentId" to review.apartmentId,
                "stayId" to review.stayId,
                "guestId" to review.guestId,
                "guestName" to review.guestName,
                "cleanliness" to review.cleanliness,
                "location" to review.location,
                "comfort" to review.comfort,
                "valueForMoney" to review.valueForMoney,
                "facilities" to review.facilities,
                "communication" to review.communication,
                "wifi" to review.wifi,
                "overallScore" to review.overallScore,
                "comment" to review.comment,
                "doodleBase64" to review.doodleBase64,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            db.collection("reviews").add(data).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating review", e)
            Resource.Error("Failed to submit review", e)
        }
    }

    override suspend fun updateReview(reviewId: String, review: Review): Resource<Unit> {
        return try {
            val data = mapOf(
                "cleanliness" to review.cleanliness,
                "location" to review.location,
                "comfort" to review.comfort,
                "valueForMoney" to review.valueForMoney,
                "facilities" to review.facilities,
                "communication" to review.communication,
                "wifi" to review.wifi,
                "overallScore" to review.overallScore,
                "comment" to review.comment,
                "doodleBase64" to review.doodleBase64,
                "updatedAt" to Timestamp.now()
            )
            db.collection("reviews").document(reviewId).update(data).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating review $reviewId", e)
            Resource.Error("Failed to update review", e)
        }
    }

    override suspend fun getRooms(apartmentId: String): Resource<List<Room>> {
        return try {
            @Suppress("UNCHECKED_CAST")
            val rooms = db.collection("apartments")
                .document(apartmentId)
                .collection("rooms")
                .get()
                .await()
                .documents
                .map { doc ->
                    Room(
                        id = doc.id,
                        appliances = doc.data
                            ?.filterValues { it is Map<*, *> }
                            ?.mapValues { (_, value) ->
                                val map = value as Map<*, *>
                                Appliance(
                                    description = map["description"] as? String ?: "",
                                    instructions = map["instructions"] as? String ?: "",
                                    images = (map["images"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                    icon = map["icon"] as? String ?: ""
                                )
                            }
                            ?: emptyMap()
                    )
                }
            Resource.Success(rooms)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching rooms for apartment $apartmentId", e)
            Resource.Error("Failed to load rooms", e)
        }
    }

    companion object {
        private const val TAG = "TouristRepo"
    }
}
