package com.example.driver.features.home.data.remote

import com.example.driver.features.home.data.remote.dto.AvailabilityRequest
import com.example.driver.features.home.data.remote.dto.AvailabilityResponse
import com.example.driver.features.home.data.remote.dto.CurrentTripResponse
import com.example.driver.features.home.data.remote.dto.OfferDto
import com.example.driver.features.home.data.remote.dto.OfferRequest
import com.example.driver.features.home.data.remote.dto.RatingRequest
import com.example.driver.features.home.data.remote.dto.StatusRequest
import com.example.driver.features.home.data.remote.dto.TripDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DriverTripsApi {
    @PUT("driver/availability")
    suspend fun availability(@Body body: AvailabilityRequest): AvailabilityResponse

    @GET("driver/trips/requests")
    suspend fun requests(): List<TripDto>

    @GET("driver/trips/current")
    suspend fun current(): CurrentTripResponse

    @POST("driver/trips/{id}/offers")
    suspend fun offer(@Path("id") tripId: String, @Body body: OfferRequest): OfferDto

    @PUT("driver/trips/{id}/status")
    suspend fun status(@Path("id") tripId: String, @Body body: StatusRequest): TripDto

    @POST("driver/trips/{id}/rating")
    suspend fun rate(@Path("id") tripId: String, @Body body: RatingRequest): TripDto
}
