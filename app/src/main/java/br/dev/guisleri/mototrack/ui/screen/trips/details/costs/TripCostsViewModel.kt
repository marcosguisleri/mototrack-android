package br.dev.guisleri.mototrack.ui.screen.trips.details.costs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.model.trip.FuelEntryRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.FuelEntryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripCostSummaryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseResponseDTO
import br.dev.guisleri.mototrack.data.network.apiErrorMessage
import br.dev.guisleri.mototrack.data.repository.TripCostRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

const val MAX_LITERS_INTEGER_DIGITS = 4
const val MAX_LITERS_FRACTION_DIGITS = 3
const val MAX_MONEY_INTEGER_DIGITS = 8
const val MAX_MONEY_FRACTION_DIGITS = 2
const val MAX_DESCRIPTION_LENGTH = 255

/**
 * The money side of one trip: fuel stops, expenses and the summary the backend
 * computes from them. Every change is followed by a reload, so the numbers the
 * rider sees are always the backend's.
 */
class TripCostsViewModel(
    private val tripId: Long,
    private val repository: TripCostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripCostsUiState())

    val uiState: StateFlow<TripCostsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load(showLoading = true)
    }

    fun retry() {
        load(showLoading = true)
    }

    /** Reloads without hiding what is on screen; used on resume and after changes. */
    fun refresh() {
        val state = _uiState.value
        if (state.isLoading || state.isSaving) return

        load(showLoading = false)
    }

    private fun load(showLoading: Boolean) {
        loadJob?.cancel()

        if (showLoading) {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                refreshErrorMessage = null
            )
        }

        loadJob = viewModelScope.launch {
            try {
                applyFetched(fetch())

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                val message = failureMessage(exception, "carregar os gastos")
                val state = _uiState.value

                _uiState.value = if (state.hasData) {
                    state.copy(isLoading = false, refreshErrorMessage = message)
                } else {
                    state.copy(isLoading = false, errorMessage = message)
                }
            }
        }
    }

    private data class Fetched(
        val summary: TripCostSummaryResponseDTO,
        val fuel: List<FuelEntryResponseDTO>,
        val expenses: List<TripExpenseResponseDTO>
    )

    private suspend fun fetch(): Fetched = coroutineScope {
        val summary = async { repository.getCostSummary(tripId) }
        val fuel = async { repository.getFuelEntries(tripId) }
        val expenses = async { repository.getExpenses(tripId) }

        Fetched(summary.await(), fuel.await(), expenses.await())
    }

    private fun applyFetched(fetched: Fetched) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = null,
            refreshErrorMessage = null,
            summary = fetched.summary,
            fuelEntries = fetched.fuel,
            expenses = fetched.expenses
        )
    }

    // -- Editor -------------------------------------------------------------

    fun addFuel() = openEditor(TripRecordEditor.NewFuel)

    fun addExpense(category: ExpenseCategory) =
        openEditor(TripRecordEditor.NewExpense(category))

    fun editRecord(record: TripRecord) = openEditor(
        when (record) {
            is TripRecord.Fuel -> TripRecordEditor.EditFuel(record.entry)
            is TripRecord.Expense -> TripRecordEditor.EditExpense(record.expense)
        }
    )

    private fun openEditor(editor: TripRecordEditor) {
        if (_uiState.value.isSaving) return

        _uiState.value = _uiState.value.copy(
            editor = editor,
            editorErrorMessage = null
        )
    }

    fun dismissEditor() {
        if (_uiState.value.isSaving) return

        _uiState.value = _uiState.value.copy(
            editor = null,
            editorErrorMessage = null
        )
    }

    fun saveFuel(litersText: String, totalCostText: String) {
        val editor = _uiState.value.editor
        if (editor !is TripRecordEditor.NewFuel && editor !is TripRecordEditor.EditFuel) return

        val liters = parseDecimalInput(litersText)
        val totalCost = parseDecimalInput(totalCostText)

        val problem = when {
            liters == null || liters <= 0.0 -> "Informe os litros abastecidos."
            totalCost == null || totalCost <= 0.0 -> "Informe o valor pago."
            else -> null
        }

        if (problem != null || liters == null || totalCost == null) {
            _uiState.value = _uiState.value.copy(editorErrorMessage = problem)
            return
        }

        val request = FuelEntryRequestDTO(liters = liters, totalCost = totalCost)

        mutate("salvar o abastecimento") {
            when (editor) {
                is TripRecordEditor.EditFuel ->
                    repository.updateFuelEntry(tripId, editor.entry.id, request)

                else -> repository.addFuelEntry(tripId, request)
            }
        }
    }

    fun saveExpense(
        category: ExpenseCategory,
        amountText: String,
        description: String
    ) {
        val editor = _uiState.value.editor
        if (editor !is TripRecordEditor.NewExpense && editor !is TripRecordEditor.EditExpense) {
            return
        }

        val amount = parseDecimalInput(amountText)

        // A toll can be exempt (a motorcycle often is), so only it may be zero.
        val problem = when {
            amount == null -> "Informe o valor."
            amount < 0.0 -> "O valor não pode ser negativo."
            amount == 0.0 && category != ExpenseCategory.TOLL ->
                "O valor deve ser maior que zero."

            else -> null
        }

        if (problem != null || amount == null) {
            _uiState.value = _uiState.value.copy(editorErrorMessage = problem)
            return
        }

        val request = TripExpenseRequestDTO(
            category = category.apiValue,
            amount = amount,
            description = description.trim()
                .take(MAX_DESCRIPTION_LENGTH)
                .takeIf { it.isNotEmpty() }
        )

        mutate("salvar o gasto") {
            when (editor) {
                is TripRecordEditor.EditExpense ->
                    repository.updateExpense(tripId, editor.expense.id, request)

                else -> repository.addExpense(tripId, request)
            }
        }
    }

    fun deleteEditedRecord() {
        val editor = _uiState.value.editor

        when (editor) {
            is TripRecordEditor.EditFuel ->
                mutate("excluir o abastecimento") {
                    repository.deleteFuelEntry(tripId, editor.entry.id)
                }

            is TripRecordEditor.EditExpense ->
                mutate("excluir o gasto") {
                    repository.deleteExpense(tripId, editor.expense.id)
                }

            else -> Unit
        }
    }

    private fun mutate(what: String, change: suspend () -> Unit) {
        val state = _uiState.value
        if (state.isSaving) return

        loadJob?.cancel()
        _uiState.value = state.copy(isSaving = true, editorErrorMessage = null)

        viewModelScope.launch {
            try {
                change()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    editorErrorMessage = failureMessage(exception, what)
                )
                return@launch
            }

            // Saved: the dialog is done even if the reload below fails.
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                editor = null,
                editorErrorMessage = null
            )

            try {
                applyFetched(fetch())
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    refreshErrorMessage = failureMessage(exception, "atualizar os gastos")
                )
            }
        }
    }

    private fun failureMessage(exception: Exception, what: String): String =
        when (exception) {
            is HttpException ->
                exception.apiErrorMessage() ?: "Não foi possível $what."

            is IOException ->
                "Não foi possível comunicar com o servidor."

            else ->
                "Não foi possível $what."
        }
}
