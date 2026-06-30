package com.touristapp.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.Timestamp
import com.touristapp.core.i18n.localize
import com.touristapp.core.i18n.localizeList
import com.touristapp.core.util.Resource
import com.touristapp.data.local.AppPreferences
import com.touristapp.data.model.*
import com.touristapp.domain.repository.TouristRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TouristRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val prefs: AppPreferences
) : TouristRepository {

    /** The language currently selected by the guest, used to resolve localized content. */
    private val lang: String get() = prefs.getLanguage()

    /**
     * Server-first read: when online, fetches fresh data from the server (and updates the local
     * cache); when offline, [Source.DEFAULT] transparently falls back to the cache. This keeps the
     * tablet in sync with admin edits while staying usable on a flaky connection. [forceServer]
     * skips the cache entirely (used for refreshes that must never read stale data).
     */
    private suspend fun fetch(query: Query, forceServer: Boolean): QuerySnapshot {
        val source = if (forceServer) Source.SERVER else Source.DEFAULT
        return query.get(source).await()
    }

    /** Single-document equivalent of [fetch]. */
    private suspend fun fetchDoc(ref: DocumentReference, forceServer: Boolean): DocumentSnapshot {
        val source = if (forceServer) Source.SERVER else Source.DEFAULT
        return ref.get(source).await()
    }

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
    override suspend fun getApartment(apartmentId: String, forceServer: Boolean): Resource<Apartment> {
        return try {
            val doc = fetchDoc(db.collection("apartments").document(apartmentId), forceServer)
            val apartment = doc.toObject(Apartment::class.java)?.copy(id = doc.id)
                ?: return Resource.Error("Apartment not found")

            val rawTransport = doc.get("transportation") as? List<Map<String, Any>> ?: emptyList()
            val transportItems = rawTransport.map { map ->
                TransportationItem(
                    type = map["type"] as? String ?: "",
                    description = localize(map["description"], lang),
                    transportationId = map["transportation_id"] as? String ?: ""
                )
            }

            val rawHouseRules = doc.get("houseRules") as? List<Map<String, Any?>> ?: emptyList()
            val houseRules = rawHouseRules.map { group ->
                HouseRuleGroup(
                    title = localize(group["title"], lang),
                    rules = localizeList(group["rules"], lang)
                )
            }

            val rawContacts = doc.get("contacts") as? List<Map<String, Any?>> ?: emptyList()
            val contacts = rawContacts.map { contact ->
                Contact(
                    name = localize(contact["name"], lang),
                    phone = contact["phone"] as? String ?: ""
                )
            }

            Resource.Success(
                apartment.copy(
                    description = localize(doc.get("description"), lang),
                    checkoutInstructions = localize(doc.get("checkoutInstructions"), lang),
                    welcomeMessage = localize(doc.get("welcomeMessage"), lang),
                    houseRules = houseRules,
                    contacts = contacts,
                    transportation = transportItems
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching apartment $apartmentId", e)
            Resource.Error("Failed to load apartment", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun getTransportationServices(serviceIds: List<String>, forceServer: Boolean): Resource<List<TransportationService>> {
        if (serviceIds.isEmpty()) return Resource.Success(emptyList())
        return try {
            val services = coroutineScope {
                serviceIds.chunked(30).map { chunk ->
                    async {
                        val refs = chunk.map { db.collection("transportation").document(it) }
                        val query = db.collection("transportation").whereIn("__name__", refs)
                        fetch(query, forceServer)
                            .documents
                            .mapNotNull { doc ->
                                val data = doc.data ?: return@mapNotNull null
                                // New flat format: { name, phone, description, thumbImageUrl? }
                                // Legacy format:   { [serviceName]: { phone, description } }
                                if (data.containsKey("name")) {
                                    TransportationService(
                                        id = doc.id,
                                        name = data["name"] as? String ?: "",
                                        phone = data["phone"] as? String ?: "",
                                        description = localize(data["description"], lang),
                                        thumbImageUrl = data["thumbImageUrl"] as? String ?: ""
                                    )
                                } else {
                                    val entry = data.entries.firstOrNull() ?: return@mapNotNull null
                                    val name = entry.key
                                    val details = entry.value as? Map<*, *> ?: return@mapNotNull null
                                    TransportationService(
                                        id = doc.id,
                                        name = name,
                                        phone = details["phone"] as? String ?: "",
                                        description = localize(details["description"], lang)
                                    )
                                }
                            }
                    }
                }.awaitAll().flatten()
            }
            Resource.Success(services)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching transportation services", e)
            Resource.Error("Failed to load transportation services", e)
        }
    }

    override suspend fun getCurrentStay(stayId: String, forceServer: Boolean): Resource<Stay> {
        return try {
            val doc = fetchDoc(db.collection("stays").document(stayId), forceServer)
            val stay = doc.toObject(Stay::class.java)?.copy(
                id = doc.id,
                welcomeMessage = localize(doc.get("welcomeMessage"), lang),
                notes = localize(doc.get("notes"), lang)
            ) ?: return Resource.Error("Stay not found")
            Resource.Success(stay)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching stay $stayId", e)
            Resource.Error("Failed to load stay", e)
        }
    }

    override suspend fun getGuest(guestId: String, forceServer: Boolean): Resource<Guest> {
        return try {
            val doc = fetchDoc(db.collection("guests").document(guestId), forceServer)
            val guest = doc.toObject(Guest::class.java)?.copy(id = doc.id)
                ?: return Resource.Error("Guest not found")
            Resource.Success(guest)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching guest $guestId", e)
            Resource.Error("Failed to load guest", e)
        }
    }

    override suspend fun getGuests(guestIds: List<String>, forceServer: Boolean): Resource<List<Guest>> {
        if (guestIds.isEmpty()) return Resource.Success(emptyList())
        return try {
            val guests = coroutineScope {
                guestIds.chunked(30).map { chunk ->
                    async {
                        val refs = chunk.map { db.collection("guests").document(it) }
                        val query = db.collection("guests").whereIn("__name__", refs)
                        fetch(query, forceServer)
                            .documents
                            .mapNotNull { doc ->
                                doc.toObject(Guest::class.java)?.copy(id = doc.id)
                            }
                    }
                }.awaitAll().flatten()
            }
            Resource.Success(guests)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching guests", e)
            Resource.Error("Failed to load guests", e)
        }
    }

    override suspend fun getPlacesForApartment(apartmentId: String, forceServer: Boolean): Resource<List<Place>> {
        return try {
            var query: Query = db.collection("places")
                .whereArrayContains("apartmentIds", apartmentId)
            if (USE_IS_ACTIVE_INDEX) {
                query = query.whereEqualTo("isActive", true)
            }
            val places = fetch(query, forceServer)
                .documents
                .mapNotNull { doc ->
                    doc.toObject(Place::class.java)?.copy(
                        id = doc.id,
                        description = localize(doc.get("description"), lang),
                        tips = localize(doc.get("tips"), lang)
                    )
                }
                .filter { USE_IS_ACTIVE_INDEX || it.isActive }
            Resource.Success(places)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching places for apartment $apartmentId", e)
            Resource.Error("Failed to load places", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun getEmergencyContactsCroatia(forceServer: Boolean): Resource<List<Contact>> {
        return try {
            val contacts = fetch(db.collection("emergency_contacts_croatia"), forceServer)
                .documents
                .flatMap { doc ->
                    val contactsList = doc.get("contacts") as? List<Map<String, Any?>> ?: emptyList()
                    contactsList.map { map ->
                        Contact(
                            name = localize(map["name"], lang),
                            phone = map["phone"] as? String ?: map["number"] as? String ?: ""
                        )
                    }
                }
            Resource.Success(contacts)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching emergency contacts", e)
            Resource.Error("Failed to load emergency contacts", e)
        }
    }

    override suspend fun getAllApartments(forceServer: Boolean): Resource<List<Pair<String, String>>> {
        return try {
            val apartments = fetch(db.collection("apartments"), forceServer)
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

    override suspend fun getRooms(apartmentId: String, forceServer: Boolean): Resource<List<Room>> {
        return try {
            @Suppress("UNCHECKED_CAST")
            val rooms = fetch(
                db.collection("apartments").document(apartmentId).collection("rooms"),
                forceServer
            )
                .documents
                .map { doc ->
                    Room(
                        id = doc.id,
                        appliances = doc.data
                            ?.filterValues { it is Map<*, *> }
                            ?.mapValues { (_, value) ->
                                val map = value as Map<*, *>
                                Appliance(
                                    description = localize(map["description"], lang),
                                    instructions = localize(map["instructions"], lang),
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

        /**
         * Set to true only once the composite index exists in the Firebase console
         * (places: apartmentIds Array-contains + isActive Asc). Until then the
         * client-side isActive filter is used to avoid a FAILED_PRECONDITION crash.
         */
        private const val USE_IS_ACTIVE_INDEX = false
    }
}
