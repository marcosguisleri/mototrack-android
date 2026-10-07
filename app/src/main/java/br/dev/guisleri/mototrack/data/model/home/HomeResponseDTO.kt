package br.dev.guisleri.mototrack.data.model.home

import kotlinx.serialization.Serializable

@Serializable
data class HomeResponseDTO(
    val nextTrip: NextTripDTO?,
    val lastCompletedTrip: TripSummaryDTO?,
    val totalCompletedDistanceKm: Double,
    val completedTrips: Long,
    val motorcycleCount: Long
)

@Serializable
data class NextTripDTO(
    val id: Long,
    val origin: String,
    val destination: String,
    val distanceKm: Double,
    val terrain: String,
    val tripDate: String,
    val daysUntil: Long,
    val motorcycle: HomeMotorcycleDTO
)

@Serializable
data class TripSummaryDTO(
    val id: Long,
    val origin: String,
    val destination: String,
    val distanceKm: Double,
    val terrain: String,
    val tripDate: String,
    val motorcycle: HomeMotorcycleDTO
)

@Serializable
data class HomeMotorcycleDTO(
    val id: Long,
    val brand: String,
    val model: String,
    val color: String,
    val year: Int,
    val engineCapacity: Int,
    val owner: HomeUserDTO
)

@Serializable
data class HomeUserDTO(
    val id: Long,
    val name: String,
    val email: String
)
