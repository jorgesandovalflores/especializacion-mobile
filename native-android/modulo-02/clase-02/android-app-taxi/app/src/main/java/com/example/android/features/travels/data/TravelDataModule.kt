package com.example.android.features.travels.data

import com.example.android.features.travels.data.api.TravelApi
import com.example.android.features.travels.data.repository.TravelRepositoryImpl
import com.example.android.features.travels.domain.repository.TravelRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// módulo Hilt propio del feature travel
@Module
@InstallIn(SingletonComponent::class)
abstract class TravelBindModule {

    // enlaza contrato → implementación
    @Binds
    @Singleton
    abstract fun bindTravelRepository(impl: TravelRepositoryImpl): TravelRepository
}

@Module
@InstallIn(SingletonComponent::class)
object TravelProvideModule {

    // TravelApi desde Retrofit global
    @Provides
    @Singleton
    fun provideTravelApi(retrofit: Retrofit): TravelApi =
        retrofit.create(TravelApi::class.java)
}