package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.model.trip.CreateTripRequestDTO
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

    suspend fun scheduleTrip(
        request: CreateTripRequestDTO
    ): TripResponseDTO {
        return tripApiService.scheduleTrip(request)
    }

    suspend fun registerCompletedTrip(
        request: CreateTripRequestDTO
    ): TripResponseDTO {
        return tripApiService.registerCompletedTrip(request)
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