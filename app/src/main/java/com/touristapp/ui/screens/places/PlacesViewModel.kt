package com.touristapp.ui.screens.places

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.touristapp.data.model.Place
import com.touristapp.data.repository.TouristRepository
import kotlinx.coroutines.launch

class PlacesViewModel(private val apartmentId: String) : ViewModel() {

    private val repo = TouristRepository()

    var places by mutableStateOf<List<Place>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var selectedCategory by mutableStateOf<String?>(null)
        private set

    val filteredPlaces: List<Place>
        get() = if (selectedCategory == null) places
                else places.filter { it.category == selectedCategory }

    val categories: List<String>
        get() = places.map { it.category }.distinct().sorted()

    init {
        loadPlaces()
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            isLoading = true
            places = repo.getPlacesForApartment(apartmentId)
            isLoading = false
        }
    }

    fun selectCategory(category: String?) {
        selectedCategory = category
    }

    class Factory(private val apartmentId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlacesViewModel(apartmentId) as T
        }
    }
}
