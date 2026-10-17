package com.touristapp.core.util

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    /** [isOffline] marks a failure caused by having no network, so the UI can say so. */
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val isOffline: Boolean = false
    ) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}

fun <T> Resource<T>.dataOrNull(): T? = when (this) {
    is Resource.Success -> data
    else -> null
}

fun <T> Resource<List<T>>.dataOrEmpty(): List<T> = when (this) {
    is Resource.Success -> data
    else -> emptyList()
}
