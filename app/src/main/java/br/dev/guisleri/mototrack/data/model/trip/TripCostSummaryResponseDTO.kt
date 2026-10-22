package br.dev.guisleri.mototrack.data.model.trip

import kotlinx.serialization.Serializable

@Serializable
data class TripCostSummaryResponseDTO(
    val fuel: FuelCostDTO,
    val expenses: List<ExpenseCategoryCostDTO>,
    // Null while nothing is recorded: no record is not the same as zero.
    val totalCost: Double? = null,
    val costPerKm: Double? = null
)

@Serializable
data class FuelCostDTO(
    val entries: Long,
    val liters: Double? = null,
    val totalCost: Double? = null
)

@Serializable
data class ExpenseCategoryCostDTO(
    val category: String,
    val count: Long,
    val total: Double
)
