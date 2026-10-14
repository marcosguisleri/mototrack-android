package br.dev.guisleri.mototrack.data.model.motorcycle

import kotlinx.serialization.Serializable

@Serializable
data class MotorcycleStatisticsResponseDTO(
    val motorcycle: MotorcycleResponseDTO,
    val completedTrips: Long,
    val totalCompletedDistanceKm: Double,
    val averageCompletedDistanceKm: Double?,
    val longestTrip: MotorcycleTripSummaryDTO?,
    val lastCompletedTrip: MotorcycleTripSummaryDTO?,
    val terrainStatistics: Map<String, TerrainStatisticsDTO>
)

@Serializable
data class MotorcycleTripSummaryDTO(
    val id: Long,
    val origin: String,
    val destination: String,
    val distanceKm: Double,
    val terrain: String,
    val tripDate: String
)

@Serializable
data class TerrainStatisticsDTO(
    val tripCount: Long,
    val totalDistanceKm: Double
)