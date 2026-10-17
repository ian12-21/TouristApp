package com.touristapp.data.local

import android.content.Context
import com.touristapp.data.model.DailyForecast
import com.touristapp.data.model.Pairing
import com.touristapp.data.model.WeatherInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
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

    /**
     * Uid of the owner this tablet is paired to. All of the owner's data lives under
     * `owners/{ownerId}/...` in Firestore, so the tablet needs it to build every path.
     * Saved together with the apartment id by [setPairing].
     */
    fun getOwnerId(): String? = prefs.getString("owner_id", null)

    /** Saves both ids in one write, so the owner and apartment can never disagree. */
    fun setPairing(pairing: Pairing) {
        prefs.edit()
            .putString("owner_id", pairing.ownerId)
            .putString("apartment_id", pairing.apartmentId)
            .apply()
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

    fun getLastWeather(): WeatherInfo? = read(KEY_WEATHER, WeatherInfo.serializer())

    fun setLastWeather(weather: WeatherInfo) {
        prefs.edit()
            .putString(KEY_WEATHER, json.encodeToString(WeatherInfo.serializer(), weather))
            .apply()
    }

    fun getLastForecast(): List<DailyForecast>? =
        read(KEY_FORECAST, ListSerializer(DailyForecast.serializer()))

    fun setLastForecast(forecast: List<DailyForecast>) {
        prefs.edit()
            .putString(KEY_FORECAST, json.encodeToString(ListSerializer(DailyForecast.serializer()), forecast))
            .apply()
    }

    /** Decodes a cached payload, discarding anything unreadable (e.g. after a model change). */
    private fun <T> read(key: String, serializer: KSerializer<T>): T? {
        val raw = prefs.getString(key, null) ?: return null
        return runCatching { json.decodeFromString(serializer, raw) }.getOrNull()
    }

    /** Forgets the cached weather, e.g. once the apartment no longer has a location. */
    fun clearWeather() {
        prefs.edit()
            .remove(KEY_WEATHER)
            .remove(KEY_FORECAST)
            .apply()
    }

    /**
     * Forgets what was cached for the previous apartment (its name and weather) when the
     * tablet is paired to another one. Pairing, theme, kiosk and language are kept.
     */
    fun clearApartmentCache() {
        prefs.edit()
            .remove("apartment_name")
            .remove(KEY_WEATHER)
            .remove(KEY_FORECAST)
            .apply()
    }

    private companion object {
        const val KEY_WEATHER = "last_weather"
        const val KEY_FORECAST = "last_forecast"
    }
}
