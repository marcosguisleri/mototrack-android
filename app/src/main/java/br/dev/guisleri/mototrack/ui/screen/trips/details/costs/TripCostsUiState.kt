package br.dev.guisleri.mototrack.ui.screen.trips.details.costs

import br.dev.guisleri.mototrack.data.model.trip.FuelEntryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripCostSummaryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseResponseDTO

/** What the rider is adding or correcting in the log, while its dialog is open. */
sealed interface TripRecordEditor {

    data object NewFuel : TripRecordEditor

    data class EditFuel(
        val entry: FuelEntryResponseDTO
    ) : TripRecordEditor

    data class NewExpense(
        val category: ExpenseCategory
    ) : TripRecordEditor

    data class EditExpense(
        val expense: TripExpenseResponseDTO
    ) : TripRecordEditor
}

data class TripCostsUiState(
    val isLoading: Boolean = true,
    // Set when nothing could be loaded; once there is data a failed refresh only warns.
    val errorMessage: String? = null,
    val refreshErrorMessage: String? = null,
    val summary: TripCostSummaryResponseDTO? = null,
    val fuelEntries: List<FuelEntryResponseDTO> = emptyList(),
    val expenses: List<TripExpenseResponseDTO> = emptyList(),
    val editor: TripRecordEditor? = null,
    val isSaving: Boolean = false,
    val editorErrorMessage: String? = null
) {
    val hasData: Boolean get() = summary != null

    val records: List<TripRecord> get() = tripRecords(fuelEntries, expenses)
}
