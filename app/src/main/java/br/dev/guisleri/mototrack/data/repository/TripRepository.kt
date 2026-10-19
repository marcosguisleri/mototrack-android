package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.data.remote.trip.TripApiService

class TripRepository (
    private val tripApiService: TripApiService
) {

    suspend fun getTrips(): List<TripResponseDTO> {
        return tripApiService.getTrips()
    }

}