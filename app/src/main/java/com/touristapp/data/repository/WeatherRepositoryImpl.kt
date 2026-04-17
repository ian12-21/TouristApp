package com.touristapp.data.repository

import android.util.Log
import com.touristapp.BuildConfig
import com.touristapp.core.util.Resource
import com.touristapp.data.model.WeatherInfo
import com.touristapp.data.model.WeatherResponse
import com.touristapp.domain.repository.WeatherRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    private val json: Json
) : WeatherRepository {

    override suspend fun getCurrentWeather(lat: Double, lon: Double): Resource<WeatherInfo> {
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

            if (httpResponse.status.value != 200) {
                Log.e(TAG, "API error ${httpResponse.status}: $body")
                return Resource.Error("Weather API error: ${httpResponse.status}")
            }

            val response = json.decodeFromString<WeatherResponse>(body)
            val condition = response.weather.firstOrNull()
            val weatherInfo = WeatherInfo(
                tempCelsius = response.main.temp,
                feelsLike = response.main.feelsLike,
                humidity = response.main.humidity,
                condition = condition?.main ?: "",
                description = condition?.description ?: "",
                iconCode = condition?.icon ?: "01d",
                cityName = response.name
            )
            Resource.Success(weatherInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch weather", e)
            Resource.Error("Failed to load weather", e)
        }
    }

    companion object {
        private const val TAG = "WeatherRepo"
    }
}
