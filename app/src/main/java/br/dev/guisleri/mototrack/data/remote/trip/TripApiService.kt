package br.dev.guisleri.mototrack.data.remote.trip

import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import retrofit2.http.GET

interface TripApiService {

    @GET("trips")
    suspend fun getTrips(): List<TripResponseDTO>

}