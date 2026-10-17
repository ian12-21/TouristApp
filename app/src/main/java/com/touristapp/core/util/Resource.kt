package com.touristapp.core.util

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    /** [reason] marks the failures the UI explains to the user; [message] is for logs. */
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val reason: ErrorReason = ErrorReason.OTHER
    ) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}

enum class ErrorReason {
    /** The device has no network and the cache couldn't serve the read. */
    OFFLINE,
    /** The server confirmed the document doesn't exist. */
    NOT_FOUND,
    /** The security rules rejected the request. */
    PERMISSION_DENIED,
    OTHER
}

fun <T> Resource<T>.dataOrNull(): T? = when (this) {
    is Resource.Success -> data
    else -> null
}

fun <T> Resource<List<T>>.dataOrEmpty(): List<T> = when (this) {
    is Resource.Success -> data
    else -> emptyList()
}
