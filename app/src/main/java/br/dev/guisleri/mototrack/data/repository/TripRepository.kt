package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.UpdateTripStatusRequestDTO
import br.dev.guisleri.mototrack.data.remote.trip.TripApiService

class TripRepository (
    private val tripApiService: TripApiService
) {

    suspend fun getTrips(): List<TripResponseDTO> {
        return tripApiService.getTrips()
    }

    suspend fun getTripById(
        tripId: Long
    ): TripResponseDTO {
        return tripApiService.getTripById(tripId)
    }

    suspend fun updateTripStatus(
        tripId: Long,
        status: String,
        updateTripDateToToday: Boolean = false
    ) {
        tripApiService.updateTripStatus(
            tripId = tripId,
            request = UpdateTripStatusRequestDTO(
                status = status,
                updateTripDateToToday = updateTripDateToToday
            )
        )
    }

}