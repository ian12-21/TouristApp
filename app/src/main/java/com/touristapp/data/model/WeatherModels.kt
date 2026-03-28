package com.touristapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

data class WeatherInfo(
    val tempCelsius: Double,
    val feelsLike: Double,
    val humidity: Int,
    val condition: String,
    val description: String,
    val iconCode: String,
    val cityName: String
)
