package com.touristapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Appliance(
    val description: String = "",
    val instructions: String = "",
    val images: List<String> = emptyList(),
    val icon: String = ""
)

data class Room(
    val id: String = "",
    val appliances: Map<String, Appliance> = emptyMap()
)

data class Contact(
    val name: String = "",
    val phone: String = ""
)

data class HouseRuleGroup(
    val title: String = "",
    val rules: List<String> = emptyList()
)

data class TransportationItem(
    val type: String = "",
    val description: String = "",
    val transportationId: String = ""
)

data class TransportationService(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val description: String = "",
    val thumbImageUrl: String = ""
)

data class Apartment(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    // Localized fields are resolved manually in the repository; excluded so
    // Firestore's toObject() never tries to coerce a {en,hr,it,de} map into a String.
    @get:Exclude val description: String = "",
    val coordinates: Map<String, Double> = emptyMap(),
    val photos: List<String> = emptyList(),
    val size: String = "",
    val capacity: Int = 0,
    val renovationYear: Int = 0,
    val wifiName: String = "",
    val wifiPassword: String = "",
    val checkoutTime: String = "",
    @get:Exclude val checkoutInstructions: String = "",
    @get:Exclude val houseRules: List<HouseRuleGroup> = emptyList(),
    @get:Exclude val contacts: List<Contact> = emptyList(),
    @get:Exclude val welcomeMessage: String = "",
    // Rebuilt manually in the repository (its description is a localized map); excluded so
    // Firestore's toObject() never tries to coerce that map into TransportationItem.description.
    @get:Exclude val transportation: List<TransportationItem> = emptyList(),
    val currentStayId: String? = null,
    val updatedAt: Timestamp? = null
)

data class Stay(
    val id: String = "",
    val guestIds: List<String> = emptyList(),
    val apartmentId: String = "",
    val checkIn: Timestamp? = null,
    val checkOut: Timestamp? = null,
    @get:Exclude val welcomeMessage: String = "",
    @get:Exclude val notes: String = "",
    val status: String = "",
    val createdAt: Timestamp? = null
)

data class Guest(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val language: String = "en",
    val active: Boolean = true,
    val createdAt: Timestamp? = null
)

/**
 * A link between a place and an apartment, with distance info.
 */
data class ApartmentLink(
    val apartmentId: String = "",
    val distance: Int = 0,
    val distanceType: String = ""
)

data class Place(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    @get:Exclude val description: String = "",
    val thumbImageUrl: String = "",
    val images: List<String> = emptyList(),
    @get:Exclude val tips: String = "",
    val phone: String = "",
    val address: String = "",
    val isActive: Boolean = true,
    val apartments: List<ApartmentLink> = emptyList(),
    val apartmentIds: List<String> = emptyList()
)

/**
 * Get the distance info for a specific apartment from a Place.
 */
fun Place.getDistanceFor(apartmentId: String): ApartmentLink? {
    return apartments.firstOrNull { it.apartmentId == apartmentId }
}

data class Review(
    val id: String = "",
    val apartmentId: String = "",
    val stayId: String = "",
    val guestId: String = "",
    val guestName: String = "",
    val cleanliness: Int = 5,
    val location: Int = 5,
    val comfort: Int = 5,
    val valueForMoney: Int = 5,
    val facilities: Int = 5,
    val communication: Int = 5,
    val wifi: Int = 5,
    val overallScore: Double = 5.0,
    val comment: String = "",
    val doodleBase64: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
