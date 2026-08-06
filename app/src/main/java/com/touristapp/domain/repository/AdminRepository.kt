package com.touristapp.domain.repository

import com.touristapp.core.util.Resource

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
     * Every apartment, as `id to "name — address"`.
     *
     * Listing `apartments` is owner-only in the security rules, so this must run
     * on the admin session; the guest session would be denied.
     */
    suspend fun getAllApartments(): Resource<List<Pair<String, String>>>

    /**
     * Binds this tablet to [apartmentId] by writing `devices/{deviceUid}`.
     *
     * This is what gives the anonymous guest session an apartment to be scoped
     * to: security rules read that document to decide which apartment's content
     * the tablet may see. Writing it requires owner rights, which is why pairing
     * can only happen here, while an owner is signed into the admin session.
     *
     * Re-pairing to a different apartment is just an overwrite.
     */
    suspend fun pairDevice(apartmentId: String): Resource<Unit>

    /** Ends the admin session. The guest session keeps running untouched. */
    fun signOut()
}
