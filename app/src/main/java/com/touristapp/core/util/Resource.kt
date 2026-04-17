package com.touristapp.core.util

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val exception: Throwable? = null) : Resource<Nothing>
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
