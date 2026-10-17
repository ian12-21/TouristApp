package com.touristapp.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.touristapp.core.di.AdminScope
import com.touristapp.core.util.Resource
import com.touristapp.core.util.toErrorReason
import com.touristapp.data.model.ApartmentChoice
import com.touristapp.data.model.Pairing
import com.touristapp.domain.repository.AdminRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    @AdminScope private val auth: FirebaseAuth,
    @AdminScope private val db: FirebaseFirestore
) : AdminRepository {

    override suspend fun signIn(email: String, password: String): Resource<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Admin sign-in failed", e)
            Resource.Error("Sign-in failed", e, reason = e.toErrorReason())
        }
    }

    override suspend fun getAllApartments(): Resource<List<ApartmentChoice>> {
        return try {
            val ownerId = auth.currentUser?.uid ?: return Resource.Error("Not signed in")
            val apartments = db.collection("owners").document(ownerId)
                .collection("apartments")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val address = doc.getString("address") ?: ""
                    ApartmentChoice(Pairing(ownerId, doc.id), label = "$name — $address")
                }
            Resource.Success(apartments)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching apartments", e)
            Resource.Error("Failed to load apartments", e, reason = e.toErrorReason())
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    private companion object {
        const val TAG = "AdminRepository"
    }
}
