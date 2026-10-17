package com.touristapp.domain.repository

import com.touristapp.core.util.Resource
import com.touristapp.data.model.ApartmentChoice

/**
 * Owner-only operations performed from the admin dialog on the tablet.
 *
 * Backed by a Firebase session entirely separate from the guest-facing one, so
 * nothing here disturbs the tablet's anonymous identity.
 */
interface AdminRepository {

    /** Signs an owner into the admin session. Does not affect the guest session. */
    suspend fun signIn(email: String, password: String): Resource<Unit>

    /**
     * The signed-in owner's apartments, each labelled "name — address".
     *
     * Each choice carries the owner's uid along with the apartment id, read while the
     * session is known to be signed in, so pairing never depends on the session still
     * being alive when the owner taps one.
     *
     * Listing apartments is owner-only in the security rules, so this must run
     * on the admin session; the guest session would be denied.
     */
    suspend fun getAllApartments(): Resource<List<ApartmentChoice>>

    /** Ends the admin session. The guest session keeps running untouched. */
    fun signOut()
}
