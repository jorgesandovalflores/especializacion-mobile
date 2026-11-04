package com.example.android.features.trip

import android.content.Context
import com.example.android.core.domain.realtime.RealtimeClient
import com.example.android.features.trip.data.location.GeocoderAddressResolver
import com.example.android.features.trip.data.remote.TripApi
import com.example.android.features.trip.data.repository.TripRepositoryImpl
import com.example.android.features.trip.domain.repository.AddressResolver
import com.example.android.features.trip.domain.repository.TripRepository
import com.example.android.features.trip.domain.usecase.AcceptOfferUseCase
import com.example.android.features.trip.domain.usecase.CancelTripUseCase
import com.example.android.features.trip.domain.usecase.GetCurrentTripUseCase
import com.example.android.features.trip.domain.usecase.ObserveTripEventsUseCase
import com.example.android.features.trip.domain.usecase.QuoteTripUseCase
import com.example.android.features.trip.domain.usecase.RateTripUseCase
import com.example.android.features.trip.domain.usecase.RequestTripUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TripModule {

    @Provides
    @Singleton
    fun provideTripApi(retrofit: Retrofit): TripApi = retrofit.create(TripApi::class.java)

    @Provides
    @Singleton
    fun provideTripRepository(api: TripApi, realtime: RealtimeClient): TripRepository =
        TripRepositoryImpl(api, realtime)

    @Provides
    @Singleton
    fun provideAddressResolver(@ApplicationContext context: Context): AddressResolver =
        GeocoderAddressResolver(context)

    @Provides
    fun provideQuoteTripUseCase(repo: TripRepository, addresses: AddressResolver) = QuoteTripUseCase(repo, addresses)

    @Provides
    fun provideRequestTripUseCase(repo: TripRepository) = RequestTripUseCase(repo)

    @Provides
    fun provideGetCurrentTripUseCase(repo: TripRepository) = GetCurrentTripUseCase(repo)

    @Provides
    fun provideAcceptOfferUseCase(repo: TripRepository) = AcceptOfferUseCase(repo)

    @Provides
    fun provideCancelTripUseCase(repo: TripRepository) = CancelTripUseCase(repo)

    @Provides
    fun provideRateTripUseCase(repo: TripRepository) = RateTripUseCase(repo)

    @Provides
    fun provideObserveTripEventsUseCase(repo: TripRepository, realtime: RealtimeClient) =
        ObserveTripEventsUseCase(repo, realtime)
}
