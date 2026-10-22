package br.dev.guisleri.mototrack.data.remote.trip

import br.dev.guisleri.mototrack.data.model.trip.CreateTripRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.UpdateTripRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.UpdateTripStatusRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.UpdateTripTimestampsRequestDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TripApiService {

    @GET("trips")
    suspend fun getTrips(): List<TripResponseDTO>

    @GET("trips/{id}")
    suspend fun getTripById(
        @Path("id") tripId: Long
    ): TripResponseDTO

    @POST("trips")
    suspend fun scheduleTrip(
        @Body request: CreateTripRequestDTO
    ): TripResponseDTO

    @POST("trips/completed")
    suspend fun registerCompletedTrip(
        @Body request: CreateTripRequestDTO
    ): TripResponseDTO

    @PUT("trips/{id}")
    suspend fun updateTrip(
        @Path("id") tripId: Long,
        @Body request: UpdateTripRequestDTO
    ): TripResponseDTO

    @PUT("trips/{id}/timestamps")
    suspend fun correctTripTimestamps(
        @Path("id") tripId: Long,
        @Body request: UpdateTripTimestampsRequestDTO
    ): TripResponseDTO

    @DELETE("trips/{id}")
    suspend fun deleteTrip(
        @Path("id") tripId: Long
    )

    @PATCH("trips/{id}/status")
    suspend fun updateTripStatus(
        @Path("id") tripId: Long,
        @Body request: UpdateTripStatusRequestDTO
    )

}