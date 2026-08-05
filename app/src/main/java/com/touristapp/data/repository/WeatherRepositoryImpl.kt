package com.touristapp.data.repository

import android.util.Log
import com.touristapp.BuildConfig
import com.touristapp.core.util.Resource
import com.touristapp.data.model.DailyForecast
import com.touristapp.data.model.ForecastResponse
import com.touristapp.data.model.WeatherInfo
import com.touristapp.data.model.WeatherResponse
import com.touristapp.data.local.AppPreferences
import com.touristapp.domain.repository.WeatherRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    private val json: Json,
    private val prefs: AppPreferences
) : WeatherRepository {

    override suspend fun getCurrentWeather(lat: Double, lon: Double): Resource<WeatherInfo> {
        val cached = prefs.getLastWeather()

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
                // 429 (rate limited) lands here — serving the cache keeps the tablet usable.
                return cached?.let { Resource.Success(it) }
                    ?: Resource.Error("Weather API error: ${httpResponse.status}")
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
            prefs.setLastWeather(weatherInfo)
            Resource.Success(weatherInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch weather", e)
            // Stale data beats no data on a wall-mounted kiosk: a guest would rather see
            // the last known temperature than a dash while the wifi is flaky.
            cached?.let { return Resource.Success(it) }
            Resource.Error("Failed to load weather", e)
        }
    }

    override suspend fun getWeekForecast(lat: Double, lon: Double): Resource<List<DailyForecast>> {
        val cached = prefs.getLastForecast()

        return try {
            val httpResponse = client.get(
                "https://api.openweathermap.org/data/2.5/forecast"
            ) {
                parameter("lat", lat)
                parameter("lon", lon)
                parameter("appid", BuildConfig.WEATHER_API_KEY)
                parameter("units", "metric")
            }

            val body = httpResponse.bodyAsText()

            if (httpResponse.status.value != 200) {
                Log.e(TAG, "Forecast API error ${httpResponse.status}: $body")
                return cached?.let { Resource.Success(it) }
                    ?: Resource.Error("Forecast API error: ${httpResponse.status}")
            }

            val response = json.decodeFromString<ForecastResponse>(body)
            val zone = ZoneId.systemDefault()

            val dailyForecasts = response.list
                .groupBy { item ->
                    Instant.ofEpochSecond(item.dt).atZone(zone).toLocalDate()
                }
                .entries
                .sortedBy { it.key }
                .map { (date, items) ->
                    val noonItem = items.minByOrNull { item ->
                        val hour = Instant.ofEpochSecond(item.dt).atZone(zone).hour
                        kotlin.math.abs(hour - 12)
                    } ?: items.first()
                    val condition = noonItem.weather.firstOrNull()
                    DailyForecast(
                        date = date,
                        minTempCelsius = items.minOf { it.main.temp },
                        maxTempCelsius = items.maxOf { it.main.temp },
                        iconCode = condition?.icon ?: "01d",
                        condition = condition?.main ?: ""
                    )
                }

            prefs.setLastForecast(dailyForecasts)
            Resource.Success(dailyForecasts)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch forecast", e)
            cached?.let { return Resource.Success(it) }
            Resource.Error("Failed to load forecast", e)
        }
    }

    companion object {
        private const val TAG = "WeatherRepo"
    }
}
