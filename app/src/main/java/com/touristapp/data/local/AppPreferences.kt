package com.touristapp.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs = context.getSharedPreferences("kiosk_prefs", Context.MODE_PRIVATE)

    fun getApartmentId(): String? = prefs.getString("apartment_id", null)

    fun setApartmentId(id: String) {
        prefs.edit().putString("apartment_id", id).apply()
    }

    fun getApartmentName(): String? = prefs.getString("apartment_name", null)

    fun setApartmentName(name: String) {
        prefs.edit().putString("apartment_name", name).apply()
    }

    fun isDarkTheme(): Boolean = prefs.getBoolean("dark_theme", true)

    fun setDarkTheme(value: Boolean) {
        prefs.edit().putBoolean("dark_theme", value).apply()
    }

    fun clear() {
        val theme = isDarkTheme()
        prefs.edit().clear().putBoolean("dark_theme", theme).apply()
    }
}
