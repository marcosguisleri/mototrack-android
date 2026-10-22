package br.dev.guisleri.mototrack.data.model.trip

import kotlinx.serialization.Serializable

@Serializable
data class TripExpenseRequestDTO(
    val category: String,
    val amount: Double,
    val description: String? = null
)

@Serializable
data class TripExpenseResponseDTO(
    val id: Long,
    val category: String,
    val amount: Double,
    val description: String? = null,
    val createdAt: String
)
