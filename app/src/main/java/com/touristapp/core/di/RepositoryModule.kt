package com.touristapp.core.di

import com.touristapp.data.repository.TouristRepositoryImpl
import com.touristapp.data.repository.WeatherRepositoryImpl
import com.touristapp.domain.repository.TouristRepository
import com.touristapp.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTouristRepository(impl: TouristRepositoryImpl): TouristRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository
}
