package br.dev.guisleri.mototrack.data.model.trip

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class TripResponseDTO(
    val id: Long,
    val origin: String,
    val destination: String,
    val distanceKm: Double,
    val status: String,
    val terrain: String,
    val tripDate: String,
    val motorcycle: MotorcycleResponseDTO,
    // ISO-8601 instants; null until the trip starts / finishes (or for old trips).
    val startedAt: String? = null,
    val completedAt: String? = null
)
