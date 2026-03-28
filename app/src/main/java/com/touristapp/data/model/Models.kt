package com.touristapp.data.model

import com.google.firebase.Timestamp

// ── Shared / Nested ──

data class Amenity(
    val name: String = "",
    val icon: String = "",
    val description: String = ""
)

data class Contact(
    val name: String = "",
    val phone: String = ""
)

/**
 * Maps to Firestore collection: apartments
 */
data class Apartment(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val coordinates: Map<String, Double> = emptyMap(),
    val photos: List<String> = emptyList(),
    val size: String = "",
    val capacity: Int = 0,
    val renovationYear: Int = 0,
    val wifiName: String = "",
    val wifiPassword: String = "",
    val checkoutTime: String = "",
    val checkoutInstructions: String = "",
    val houseRules: List<String> = emptyList(),
    val amenities: List<Amenity> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val welcomeMessage: String = "",
    val transportTips: String = "",
    val currentStayId: String? = null,
    val placeIds: List<String> = emptyList(),
    val updatedAt: Timestamp? = null
)

/**
 * Maps to Firestore collection: stays
 */
data class Stay(
    val id: String = "",
    val guestIds: List<String> = emptyList(),
    val apartmentId: String = "",
    val checkIn: Timestamp? = null,
    val checkOut: Timestamp? = null,
    val welcomeMessage: String = "",
    val notes: String = "",
    val status: String = "",
    val createdAt: Timestamp? = null
)

/**
 * Maps to Firestore collection: guests
 */
data class Guest(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val language: String = "en",
    val createdAt: Timestamp? = null
)

/**
 * Maps to Firestore collection: places
 */
data class Place(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val coordinates: Map<String, Double> = emptyMap(),
    val address: String = "",
    val photoUrl: String = "",
    val rating: Double = 0.0,
    val tips: String = "",
    val website: String = "",
    val phone: String = "",
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null
)

/**
 * Maps to Firestore collection: customSections
 */
data class CustomSection(
    val id: String = "",
    val title: String = "",
    val icon: String = "",
    val content: String = "",
    val order: Int = 0,
    val isActive: Boolean = true
)

/**
 * Maps to Firestore collection: admins
 */
data class Admin(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val apartmentIds: List<String> = emptyList(),
    val createdAt: Timestamp? = null
)
