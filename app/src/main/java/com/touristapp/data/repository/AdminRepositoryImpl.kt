package com.touristapp.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.touristapp.core.di.AdminScope
import com.touristapp.core.util.Resource
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
            Resource.Error("Invalid credentials", e)
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

    override fun signOut() {
        auth.signOut()
    }

    private companion object {
        const val TAG = "AdminRepository"
    }
}
