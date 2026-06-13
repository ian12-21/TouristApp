package com.touristapp.domain.repository

import com.touristapp.core.util.Resource
import com.touristapp.data.model.DailyForecast
import com.touristapp.data.model.WeatherInfo

interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Resource<WeatherInfo>
    suspend fun getWeekForecast(lat: Double, lon: Double): Resource<List<DailyForecast>>
}
