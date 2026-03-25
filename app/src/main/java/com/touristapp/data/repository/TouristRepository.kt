package com.touristapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.touristapp.data.model.*
import kotlinx.coroutines.tasks.await

/**
 * Read-only repository for Firestore data.
 * The tablet app never writes to Firestore — only the admin panel does.
 */
class TouristRepository {

    private val db = FirebaseFirestore.getInstance()

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
}
