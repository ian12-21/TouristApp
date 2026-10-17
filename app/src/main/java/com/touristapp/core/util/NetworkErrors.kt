package com.touristapp.core.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * True when a Firebase call failed because the device has no network: Firestore reports
 * a read it can't serve from the cache as UNAVAILABLE ("the client is offline"), and
 * Auth throws [FirebaseNetworkException].
 */
fun Throwable.isOfflineError(): Boolean = when (this) {
    is FirebaseNetworkException -> true
    is FirebaseFirestoreException -> code == FirebaseFirestoreException.Code.UNAVAILABLE
    else -> false
}

/** [ErrorReason] for an exception caught at a repository boundary. */
fun Throwable.toErrorReason(): ErrorReason =
    if (isOfflineError()) ErrorReason.OFFLINE else ErrorReason.OTHER
