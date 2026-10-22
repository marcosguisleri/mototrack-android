package br.dev.guisleri.mototrack.data.model.trip

import kotlinx.serialization.Serializable

@Serializable
data class FuelEntryRequestDTO(
    val liters: Double,
    val totalCost: Double
)

@Serializable
data class FuelEntryResponseDTO(
    val id: Long,
    val liters: Double,
    val totalCost: Double,
    val pricePerLiter: Double,
    val createdAt: String
)
