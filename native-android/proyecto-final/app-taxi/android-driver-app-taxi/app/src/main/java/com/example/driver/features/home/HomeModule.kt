package com.example.driver.features.home

import android.content.Context
import android.location.LocationManager
import com.example.driver.commons.domain.store.DriverStore
import com.example.driver.core.domain.LocalCache
import com.example.driver.core.domain.SessionStore
import com.example.driver.core.domain.realtime.RealtimeGateway
import com.example.driver.features.home.data.location.LocationRepositoryImpl
import com.example.driver.features.home.data.realtime.DriverLocationPublisherImpl
import com.example.driver.features.home.data.realtime.TripEventsRepositoryImpl
import com.example.driver.features.home.data.remote.DriverTripsApi
import com.example.driver.features.home.data.repository.TripRepositoryImpl
import com.example.driver.features.home.data.tracking.LocationTrackerImpl
import com.example.driver.features.home.domain.repository.DriverLocationPublisher
import com.example.driver.features.home.domain.repository.LocationRepository
import com.example.driver.features.home.domain.repository.LocationTracker
import com.example.driver.features.home.domain.repository.TripEventsRepository
import com.example.driver.features.home.domain.repository.TripRepository
import com.example.driver.features.home.domain.usecase.AdvanceTripUseCase
import com.example.driver.features.home.domain.usecase.AvailabilityUseCase
import com.example.driver.features.home.domain.usecase.GetCurrentTripUseCase
import com.example.driver.features.home.domain.usecase.GetTripRequestsUseCase
import com.example.driver.features.home.domain.usecase.ObserveLocationUseCase
import com.example.driver.features.home.domain.usecase.RateTripUseCase
import com.example.driver.features.home.domain.usecase.SendOfferUseCase
import com.example.driver.features.home.domain.usecase.SignOutUseCase
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
    @Singleton
    fun provideDriverTripsApi(retrofit: Retrofit): DriverTripsApi =
        retrofit.create(DriverTripsApi::class.java)

    @Provides
    @Singleton
    fun provideTripRepository(api: DriverTripsApi): TripRepository = TripRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideTripEventsRepository(gateway: RealtimeGateway): TripEventsRepository =
        TripEventsRepositoryImpl(gateway)

    @Provides
    @Singleton
    fun provideDriverLocationPublisher(gateway: RealtimeGateway): DriverLocationPublisher =
        DriverLocationPublisherImpl(gateway)

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
    fun provideObserveLocationUseCase(repo: LocationRepository): ObserveLocationUseCase =
        ObserveLocationUseCase(repo)

    @Provides
    fun provideAvailabilityUseCase(repo: TripRepository, store: DriverStore): AvailabilityUseCase =
        AvailabilityUseCase(repo, store)

    @Provides
    fun provideGetTripRequestsUseCase(repo: TripRepository): GetTripRequestsUseCase =
        GetTripRequestsUseCase(repo)

    @Provides
    fun provideGetCurrentTripUseCase(repo: TripRepository): GetCurrentTripUseCase =
        GetCurrentTripUseCase(repo)

    @Provides
    fun provideSendOfferUseCase(repo: TripRepository): SendOfferUseCase = SendOfferUseCase(repo)

    @Provides
    fun provideAdvanceTripUseCase(repo: TripRepository): AdvanceTripUseCase = AdvanceTripUseCase(repo)

    @Provides
    fun provideRateTripUseCase(repo: TripRepository): RateTripUseCase = RateTripUseCase(repo)

    @Provides
    fun provideSignOutUseCase(
        repo: TripRepository,
        store: DriverStore,
        tracker: LocationTracker,
        events: TripEventsRepository,
        session: SessionStore,
        localCache: LocalCache
    ): SignOutUseCase = SignOutUseCase(repo, store, tracker, events, session, localCache)
}
