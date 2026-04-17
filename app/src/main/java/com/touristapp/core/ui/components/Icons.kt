package com.touristapp.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.touristapp.data.model.Appliance
import kotlin.math.absoluteValue

// ── Shared palette ──

val appliancePalette = listOf(
    Color(0xFF4ECDC4), // Teal
    Color(0xFF6CA0DC), // Blue
    Color(0xFFFFB74D), // Amber
    Color(0xFFEF9A9A), // Pink
    Color(0xFFB39DDB), // Purple
    Color(0xFF81C784)  // Green
)

// ── Appliance icons ──

val materialIconMap: Map<String, ImageVector> = mapOf(
    "Tv" to Icons.Default.Tv,
    "Coffee" to Icons.Default.Coffee,
    "LocalLaundryService" to Icons.Default.LocalLaundryService,
    "Thermostat" to Icons.Default.Thermostat,
    "LightMode" to Icons.Default.LightMode,
    "Kitchen" to Icons.Default.Kitchen,
    "Microwave" to Icons.Default.Microwave,
    "Wifi" to Icons.Default.Wifi,
    "Water" to Icons.Default.Water,
    "Bed" to Icons.Default.Bed,
    "Lock" to Icons.Default.Lock,
    "Air" to Icons.Default.Air,
    "Devices" to Icons.Default.Devices,
    "Dishwasher" to Icons.Default.DinnerDining,
    "Iron" to Icons.Default.Iron,
    "Shower" to Icons.Default.Shower,
    "Bathtub" to Icons.Default.Bathtub,
    "Speaker" to Icons.Default.Speaker,
    "Router" to Icons.Default.Router,
    "Power" to Icons.Default.Power,
    "Fireplace" to Icons.Default.Fireplace,
    "Blender" to Icons.Default.Blender,
    "Chair" to Icons.Default.Chair,
    "Garage" to Icons.Default.Garage,
    "Pool" to Icons.Default.Pool,
    "FitnessCenter" to Icons.Default.FitnessCenter,
    "Yard" to Icons.Default.Yard,
    "HotTub" to Icons.Default.HotTub,
    "DryCleaning" to Icons.Default.DryCleaning,
    "Balcony" to Icons.Default.Balcony,
    "Elevator" to Icons.Default.Elevator,
    "CoffeeMaker" to Icons.Default.CoffeeMaker,
    "Countertops" to Icons.Default.Countertops,
    "Outlet" to Icons.Default.Outlet,
    "MeetingRoom" to Icons.Default.MeetingRoom
)

fun keywordIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        "tv" in lower || "television" in lower -> Icons.Default.Tv
        "coffee" in lower -> Icons.Default.Coffee
        "wash" in lower || "laundry" in lower -> Icons.Default.LocalLaundryService
        "heat" in lower || "ac" in lower || "air condition" in lower || "thermostat" in lower -> Icons.Default.Thermostat
        "light" in lower || "lamp" in lower -> Icons.Default.LightMode
        "fridge" in lower || "refrigerator" in lower -> Icons.Default.Kitchen
        "oven" in lower || "stove" in lower || "microwave" in lower -> Icons.Default.Microwave
        "wifi" in lower || "internet" in lower || "router" in lower -> Icons.Default.Wifi
        "shower" in lower || "bath" in lower || "water" in lower -> Icons.Default.Water
        "bed" in lower || "mattress" in lower -> Icons.Default.Bed
        "lock" in lower || "key" in lower || "door" in lower -> Icons.Default.Lock
        "fan" in lower || "ventilat" in lower -> Icons.Default.Air
        "iron" in lower -> Icons.Default.Iron
        "dishwash" in lower -> Icons.Default.DinnerDining
        "speaker" in lower || "sound" in lower -> Icons.Default.Speaker
        "pool" in lower -> Icons.Default.Pool
        else -> Icons.Default.Devices
    }
}

fun resolveApplianceIcon(name: String, appliance: Appliance): Pair<ImageVector, Color> {
    val icon = when {
        appliance.icon.isNotEmpty() -> materialIconMap[appliance.icon] ?: keywordIcon(name)
        else -> keywordIcon(name)
    }
    val color = appliancePalette[name.hashCode().absoluteValue % appliancePalette.size]
    return icon to color
}

// ── House rule group icons ──

fun ruleGroupIcon(title: String): ImageVector {
    val lower = title.lowercase()
    return when {
        "check-in" in lower || "key" in lower -> Icons.Default.Key
        "guest" in lower || "occupancy" in lower -> Icons.Default.Groups
        "noise" in lower || "quiet" in lower -> Icons.Default.VolumeOff
        "clean" in lower || "tidy" in lower -> Icons.Default.CleaningServices
        "parking" in lower || "car" in lower -> Icons.Default.LocalParking
        "pet" in lower || "animal" in lower -> Icons.Default.Pets
        "smoke" in lower -> Icons.Default.SmokeFree
        "pool" in lower || "swim" in lower -> Icons.Default.Pool
        "trash" in lower || "waste" in lower || "recycle" in lower -> Icons.Default.Delete
        "safety" in lower || "security" in lower || "emergency" in lower -> Icons.Default.Security
        "kitchen" in lower || "cook" in lower -> Icons.Default.Kitchen
        "laundry" in lower || "wash" in lower -> Icons.Default.LocalLaundryService
        else -> Icons.Default.Gavel
    }
}
