package com.example.android.features.trip.data.remote

import com.example.android.features.trip.data.remote.dto.CurrentTripDto
import com.example.android.features.trip.data.remote.dto.QuoteDto
import com.example.android.features.trip.data.remote.dto.RatingRequest
import com.example.android.features.trip.data.remote.dto.TripDto
import com.example.android.features.trip.data.remote.dto.TripRouteRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TripApi {
    @POST("trips/quote")
    suspend fun quote(@Body request: TripRouteRequest): QuoteDto

    @POST("trips")
    suspend fun request(@Body request: TripRouteRequest): TripDto

    @GET("trips/current")
    suspend fun current(): CurrentTripDto

    @POST("trips/{id}/offers/{offerId}/accept")
    suspend fun accept(@Path("id") tripId: String, @Path("offerId") offerId: String): TripDto

    @POST("trips/{id}/cancel")
    suspend fun cancel(@Path("id") tripId: String): TripDto

    @POST("trips/{id}/rating")
    suspend fun rate(@Path("id") tripId: String, @Body request: RatingRequest): TripDto
}
