package com.touristapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.touristapp.data.model.*
import kotlinx.coroutines.tasks.await
import android.util.Log

class TouristRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun ensureAnonymousAuth() {
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
                Log.d("ReviewAuth", "Anonymous auth success, uid=${auth.currentUser?.uid}")
            } catch (e: Exception) {
                Log.e("ReviewAuth", "Anonymous auth failed", e)
            }
        } else {
            Log.d("ReviewAuth", "Already authenticated, uid=${auth.currentUser?.uid}")
        }
    }

    suspend fun getApartment(apartmentId: String): Apartment? {
        return try {
            val doc = db.collection("apartments").document(apartmentId).get().await()
            doc.toObject(Apartment::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCurrentStay(stayId: String): Stay? {
        return try {
            val doc = db.collection("stays").document(stayId).get().await()
            doc.toObject(Stay::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getGuest(guestId: String): Guest? {
        return try {
            val doc = db.collection("guests").document(guestId).get().await()
            doc.toObject(Guest::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPlaces(placeIds: List<String>): List<Place> {
        if (placeIds.isEmpty()) return emptyList()
        return try {
            // Firestore 'in' queries support max 30 items per batch
            placeIds.chunked(30).flatMap { chunk ->
                db.collection("places")
                    .whereIn("__name__", chunk.map { db.collection("places").document(it) })
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(Place::class.java)?.copy(id = doc.id)
                    }
                    .filter { it.isActive }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // suspend fun getAdminApartmentIds(adminUid: String): List<String> {
    //     return try {
    //         val doc = db.collection("admins").document(adminUid).get().await()
    //         doc.toObject(Admin::class.java)?.apartmentIds ?: emptyList()
    //     } catch (e: Exception) {
    //         emptyList()
    //     }
    // }

    @Suppress("UNCHECKED_CAST")
    suspend fun getEmergencyContactsCroatia(): List<Contact> {
        return try {
            db.collection("emergency_contacts_croatia")
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
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllApartments(): List<Pair<String, String>> {
        return try {
            db.collection("apartments")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val address = doc.getString("address") ?: ""
                    doc.id to "$name — $address"
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getReviewsForApartment(apartmentId: String): List<Review> {
        return try {
            db.collection("reviews")
                .whereEqualTo("apartmentId", apartmentId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(Review::class.java)?.copy(id = doc.id)
                }
        } catch (e: Exception) {
            Log.e("TouristRepository", "Failed to fetch reviews", e)
            emptyList()
        }
    }

    suspend fun getReviewForGuestAndStay(guestId: String, stayId: String): Review? {
        return try {
            val docs = db.collection("reviews")
                .whereEqualTo("guestId", guestId)
                .whereEqualTo("stayId", stayId)
                .get()
                .await()
                .documents
            docs.firstOrNull()?.let { doc ->
                doc.toObject(Review::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("TouristRepository", "Failed to check existing review", e)
            null
        }
    }

    suspend fun createReview(review: Review): Boolean {
        Log.d("ReviewRepo", "createReview called — auth.currentUser=${auth.currentUser?.uid}")
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
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            val docRef = db.collection("reviews").add(data).await()
            Log.d("ReviewRepo", "Review created successfully, docId=${docRef.id}")
            true
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Failed to create review", e)
            false
        }
    }

    suspend fun updateReview(reviewId: String, review: Review): Boolean {
        Log.d("ReviewRepo", "updateReview called — reviewId=$reviewId, auth.currentUser=${auth.currentUser?.uid}")
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
                "updatedAt" to Timestamp.now()
            )
            db.collection("reviews").document(reviewId).update(data).await()
            Log.d("ReviewRepo", "Review updated successfully")
            true
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Failed to update review", e)
            false
        }
    }
}
