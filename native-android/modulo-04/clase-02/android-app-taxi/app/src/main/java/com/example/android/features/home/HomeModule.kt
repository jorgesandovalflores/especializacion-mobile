package com.example.android.features.home

import android.content.Context
import android.location.LocationManager
import com.example.android.core.domain.LocalCache
import com.example.android.core.domain.SessionStore
import com.example.android.features.home.data.location.LocationRepositoryImpl
import com.example.android.features.home.data.tracking.LocationTrackerImpl
import com.example.android.features.home.domain.repository.LocationRepository
import com.example.android.features.home.domain.repository.LocationTracker
import com.example.android.features.home.domain.usecase.ObserveLocationUseCase
import com.example.android.features.home.domain.usecase.SignOutUseCase
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    fun provideSignOutUseCase(session: SessionStore, localCache: LocalCache): SignOutUseCase =
        SignOutUseCase(session, localCache)

    @Provides
    @Singleton
    fun provideLocationRepository(@ApplicationContext context: Context): LocationRepository =
        LocationRepositoryImpl(
            client = LocationServices.getFusedLocationProviderClient(context),
            locationManager = context.getSystemService(LocationManager::class.java)
        )

    @Provides
    @Singleton
    fun provideLocationTracker(@ApplicationContext context: Context): LocationTracker =
        LocationTrackerImpl(context)

    @Provides
    @Singleton
    fun provideObserveLocationUseCase(repo: LocationRepository): ObserveLocationUseCase =
        ObserveLocationUseCase(repo)
}
