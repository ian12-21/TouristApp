package com.touristapp.data.model

/**
 * Maps to Firestore collection: apartments
 */
data class Apartment(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val coordinates: Map<String, Double> = emptyMap(),
    val wifi: Map<String, String> = emptyMap(),
    val amenities: List<String> = emptyList(),
    val houseRules: List<String> = emptyList(),
    val checkoutInstructions: String = "",
    val emergencyContacts: List<Map<String, String>> = emptyList(),
    val transportTips: String = "",
    val currentStayId: String? = null,
    val placeIds: List<String> = emptyList()
)

/**
 * Maps to Firestore collection: stays
 */
data class Stay(
    val id: String = "",
    val guestId: String = "",
    val apartmentId: String = "",
    val checkIn: String = "",
    val checkOut: String = "",
    val status: String = "",
    val welcomeMessage: String = ""
)

/**
 * Maps to Firestore collection: guests
 */
data class Guest(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val language: String = "en"
)

/**
 * Maps to Firestore collection: places
 */
data class Place(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val coordinates: Map<String, Double> = emptyMap(),
    val description: String = "",
    val tips: String = "",
    val contact: String = "",
    val isActive: Boolean = true
)

/**
 * Maps to Firestore collection: admins
 * Used only during setup to fetch apartment list.
 */
data class Admin(
    val id: String = "",
    val apartmentIds: List<String> = emptyList()
)
