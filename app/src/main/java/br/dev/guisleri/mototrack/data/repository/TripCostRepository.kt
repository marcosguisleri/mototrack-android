package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.model.trip.FuelEntryRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.FuelEntryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripCostSummaryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseResponseDTO
import br.dev.guisleri.mototrack.data.remote.trip.TripCostApiService

/** Everything a trip costs: the fuel stops, the other expenses and their summary. */
class TripCostRepository(
    private val api: TripCostApiService
) {

    suspend fun getCostSummary(tripId: Long): TripCostSummaryResponseDTO =
        api.getCostSummary(tripId)

    suspend fun getFuelEntries(tripId: Long): List<FuelEntryResponseDTO> =
        api.getFuelEntries(tripId)

    suspend fun getExpenses(tripId: Long): List<TripExpenseResponseDTO> =
        api.getExpenses(tripId)

    suspend fun addFuelEntry(
        tripId: Long,
        request: FuelEntryRequestDTO
    ): FuelEntryResponseDTO =
        api.addFuelEntry(tripId, request)

    suspend fun updateFuelEntry(
        tripId: Long,
        fuelEntryId: Long,
        request: FuelEntryRequestDTO
    ): FuelEntryResponseDTO =
        api.updateFuelEntry(tripId, fuelEntryId, request)

    suspend fun deleteFuelEntry(tripId: Long, fuelEntryId: Long) {
        api.deleteFuelEntry(tripId, fuelEntryId)
    }

    suspend fun addExpense(
        tripId: Long,
        request: TripExpenseRequestDTO
    ): TripExpenseResponseDTO =
        api.addExpense(tripId, request)

    suspend fun updateExpense(
        tripId: Long,
        expenseId: Long,
        request: TripExpenseRequestDTO
    ): TripExpenseResponseDTO =
        api.updateExpense(tripId, expenseId, request)

    suspend fun deleteExpense(tripId: Long, expenseId: Long) {
        api.deleteExpense(tripId, expenseId)
    }

}
