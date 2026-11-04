package com.example.android.features.home

import android.content.Context
import android.location.LocationManager
import com.example.android.core.domain.LocalCache
import com.example.android.core.domain.SessionStore
import com.example.android.features.home.data.location.LocationRepositoryImpl
import com.example.android.features.home.data.push.DeviceApi
import com.example.android.features.home.data.push.PushNotifier
import com.example.android.features.home.data.push.PushTokenRepositoryImpl
import com.example.android.features.home.data.tracking.LocationTrackerImpl
import com.example.android.features.home.domain.repository.LocationRepository
import com.example.android.features.home.domain.repository.LocationTracker
import com.example.android.features.home.domain.repository.PushTokenRepository
import com.example.android.features.home.domain.usecase.ObserveLocationUseCase
import com.example.android.features.home.domain.usecase.RegisterPushTokenUseCase
import com.example.android.features.home.domain.usecase.SignOutUseCase
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    fun provideSignOutUseCase(
        session: SessionStore,
        localCache: LocalCache,
        push: PushTokenRepository
    ): SignOutUseCase = SignOutUseCase(session, localCache, push)

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

    @Provides
    @Singleton
    fun provideDeviceApi(retrofit: Retrofit): DeviceApi =
        retrofit.create(DeviceApi::class.java)

    @Provides
    @Singleton
    fun providePushTokenRepository(@ApplicationContext context: Context, api: DeviceApi): PushTokenRepository =
        PushTokenRepositoryImpl(context, api)

    @Provides
    fun provideRegisterPushTokenUseCase(repo: PushTokenRepository, session: SessionStore): RegisterPushTokenUseCase =
        RegisterPushTokenUseCase(repo, session)

    @Provides
    @Singleton
    fun providePushNotifier(@ApplicationContext context: Context): PushNotifier =
        PushNotifier(context)
}
