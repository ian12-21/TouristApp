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

    /**
     * The owner's own apartments, for the pairing screen.
     *
     * Scoped by `ownerUid`: without the filter this listed every apartment in the
     * project, so a second owner setting up their tablet would see — and could
     * pair with — someone else's properties. Security rules now reject an
     * unscoped list outright, so this filter is required for the query to run at
     * all, not merely to tidy the results.
     */
    override suspend fun getAllApartments(): Resource<List<Pair<String, String>>> {
        return try {
            val ownerUid = auth.currentUser?.uid
                ?: return Resource.Error("Not signed in")

            val apartments = db.collection("apartments")
                .whereEqualTo("ownerUid", ownerUid)
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
