package com.touristapp.data.local

import android.content.Context

/**
 * Simple wrapper around SharedPreferences.
 * Stores only the apartment ID — this is the source of truth
 * for which apartment this tablet displays.
 */
class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("kiosk_prefs", Context.MODE_PRIVATE)

    fun getApartmentId(): String? = prefs.getString("apartment_id", null)

    fun setApartmentId(id: String) {
        prefs.edit().putString("apartment_id", id).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
