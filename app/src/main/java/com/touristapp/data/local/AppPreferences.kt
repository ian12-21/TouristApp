package com.touristapp.data.local

import android.content.Context
import com.touristapp.data.model.WeatherInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json
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

    fun getLanguage(): String = prefs.getString("language", "en") ?: "en"

    fun setLanguage(code: String) {
        prefs.edit().putString("language", code).apply()
    }

    // Kiosk is off until the owner explicitly turns it on from the admin dialog.
    fun isKioskEnabled(): Boolean = prefs.getBoolean("kiosk_enabled", false)

    fun setKioskEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("kiosk_enabled", enabled).apply()
    }

    fun getLastWeather(): WeatherInfo? {
        val raw = prefs.getString("last_weather", null) ?: return null
        return runCatching {
            json.decodeFromString(WeatherInfo.serializer(), raw)
        }.getOrNull()
    }

    fun setLastWeather(weather: WeatherInfo) {
        val raw = json.encodeToString(WeatherInfo.serializer(), weather)
        prefs.edit().putString("last_weather", raw).apply()
    }

    fun clear() {
        // Preserve device-level settings the owner controls; reconfiguring an apartment
        // must not silently flip them.
        val theme = isDarkTheme()
        val kiosk = isKioskEnabled()
        val language = getLanguage()
        prefs.edit().clear()
            .putBoolean("dark_theme", theme)
            .putBoolean("kiosk_enabled", kiosk)
            .putString("language", language)
            .apply()
    }
}
