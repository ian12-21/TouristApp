package com.touristapp.data.repository

import android.util.Log
import com.touristapp.BuildConfig
import com.touristapp.data.model.WeatherInfo
import com.touristapp.data.model.WeatherResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class WeatherRepository {

    private val client = HttpClient(Android)

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherInfo? {
        return try {
            val httpResponse = client.get(
                "https://api.openweathermap.org/data/2.5/weather"
            ) {
                parameter("lat", lat)
                parameter("lon", lon)
                parameter("appid", BuildConfig.WEATHER_API_KEY)
                parameter("units", "metric")
            }

            val body = httpResponse.bodyAsText()
            Log.d("WeatherRepository", "Response: $body")

            if (httpResponse.status.value != 200) {
                Log.e("WeatherRepository", "API error ${httpResponse.status}: $body")
                return null
            }

            val response = json.decodeFromString<WeatherResponse>(body)
            val condition = response.weather.firstOrNull()
            WeatherInfo(
                tempCelsius = response.main.temp,
                feelsLike = response.main.feelsLike,
                humidity = response.main.humidity,
                condition = condition?.main ?: "",
                description = condition?.description ?: "",
                iconCode = condition?.icon ?: "01d",
                cityName = response.name
            )
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Failed to fetch weather", e)
            null
        }
    }
}
