package com.touristapp.data.model

import com.touristapp.core.util.LocalDateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class WeatherResponse(
    val main: WeatherMain,
    val weather: List<WeatherCondition>,
    val name: String
)

@Serializable
data class WeatherMain(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    val humidity: Int
)

@Serializable
data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@Serializable
data class WeatherInfo(
    val tempCelsius: Double,
    val feelsLike: Double,
    val humidity: Int,
    val condition: String,
    val description: String,
    val iconCode: String,
    val cityName: String
)

@Serializable
data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: ForecastCity
)

@Serializable
data class ForecastItem(
    val dt: Long,
    val main: WeatherMain,
    val weather: List<WeatherCondition>
)

@Serializable
data class ForecastCity(
    val name: String
)

// @Serializable so the forecast can be cached to disk alongside the current weather —
// otherwise the 5-day strip is empty on every cold start until the first network call lands.
@Serializable
data class DailyForecast(
    @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
    val minTempCelsius: Double,
    val maxTempCelsius: Double,
    val iconCode: String,
    val condition: String
)

/**
 * Today's row covers only the 3-hour slots still ahead, so late in the day the temperature
 * right now can fall outside it ("12°–12°" while it's 11°). Widen today's range to include it.
 */
fun List<DailyForecast>.includingCurrent(tempCelsius: Double, today: LocalDate): List<DailyForecast> =
    map { day ->
        if (day.date != today) day
        else day.copy(
            minTempCelsius = minOf(day.minTempCelsius, tempCelsius),
            maxTempCelsius = maxOf(day.maxTempCelsius, tempCelsius)
        )
    }
