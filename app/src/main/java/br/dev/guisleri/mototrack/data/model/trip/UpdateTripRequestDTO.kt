package br.dev.guisleri.mototrack.data.model.trip

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTripRequestDTO(
    val origin: String,
    val destination: String,
    val distanceKm: Double,
    val terrain: String,
    val tripDate: String,
    val motorcycleId: Long
)
