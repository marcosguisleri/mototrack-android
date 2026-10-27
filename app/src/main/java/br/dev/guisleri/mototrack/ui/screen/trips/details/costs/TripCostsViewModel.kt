package br.dev.guisleri.mototrack.ui.screen.trips.details.costs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.model.trip.FuelEntryRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.FuelEntryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripCostSummaryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseResponseDTO
import br.dev.guisleri.mototrack.data.network.apiErrorMessage
import br.dev.guisleri.mototrack.data.offline.PendingRecord
import br.dev.guisleri.mototrack.data.offline.PendingRecordStore
import br.dev.guisleri.mototrack.data.offline.PendingRecordSyncer
import br.dev.guisleri.mototrack.data.repository.TripCostRepository
import br.dev.guisleri.mototrack.ui.feedback.Feedback
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

const val MAX_LITERS_INTEGER_DIGITS = 4
const val MAX_LITERS_FRACTION_DIGITS = 3
const val MAX_MONEY_INTEGER_DIGITS = 8
const val MAX_MONEY_FRACTION_DIGITS = 2
const val MAX_DESCRIPTION_LENGTH = 255

/**
 * The money side of one trip: fuel stops, expenses and the summary the backend
 * computes from them. Every change is followed by a reload, so the numbers the
 * rider sees are always the backend's.
 *
 * With a [pendingStore], a new record registered without a connection is kept on the
 * device and counted in the totals at once; the [syncer] sends it when the connection
 * is back.
 */
class TripCostsViewModel(
    private val tripId: Long,
    private val repository: TripCostRepository,
    private val pendingStore: PendingRecordStore? = null,
    private val syncer: PendingRecordSyncer? = null,
    private val now: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripCostsUiState())

    val uiState: StateFlow<TripCostsUiState> = _uiState.asStateFlow()

    // One-off messages, buffered so none is lost while nobody is collecting.
    private val _feedback = Channel<Feedback>(Channel.BUFFERED)

    val feedback: Flow<Feedback> = _feedback.receiveAsFlow()

    private var loadJob: Job? = null

    // What the server said, before the pending records are added to it.
    private var remoteSummary: TripCostSummaryResponseDTO? = null
    private var tripDistanceKm: Double? = null
    private var lastPendingCount = 0

    init {
        observePending()
        load(showLoading = true)
        sendPending()
    }

    private fun observePending() {
        val store = pendingStore ?: return

        viewModelScope.launch {
            store.observe().collect { all ->
                val mine = all.filter { it.tripId == tripId }
                val someWereSent = mine.size < lastPendingCount
                lastPendingCount = mine.size

                _uiState.value = withMergedSummary(_uiState.value.copy(pending = mine))

                // What left the queue is now in the server's numbers.
                if (someWereSent) refresh()
            }
        }
    }

    private fun sendPending() {
        val sender = syncer ?: return

        viewModelScope.launch { sender.flush() }
    }

    /** The trip's distance, to give the cost per km of what is still pending. */
    fun onTripDistanceKnown(distanceKm: Double) {
        tripDistanceKm = distanceKm
        _uiState.value = withMergedSummary(_uiState.value)
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        load(showLoading = true)
        sendPending()
    }

    /** Reloads without hiding what is on screen; used on resume and after changes. */
    fun refresh() {
        sendPending()

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
        remoteSummary = fetched.summary

        _uiState.value = withMergedSummary(
            _uiState.value.copy(
                isLoading = false,
                errorMessage = null,
                refreshErrorMessage = null,
                fuelEntries = fetched.fuel,
                expenses = fetched.expenses
            )
        )
    }

    private fun withMergedSummary(state: TripCostsUiState): TripCostsUiState =
        state.copy(summary = mergePending(remoteSummary, state.pending, tripDistanceKm))

    // -- Editor -------------------------------------------------------------

    fun addFuel() = openEditor(TripRecordEditor.NewFuel)

    fun addExpense(category: ExpenseCategory) =
        openEditor(TripRecordEditor.NewExpense(category))

    fun editRecord(record: TripRecord) = openEditor(
        when (record) {
            is TripRecord.Fuel -> TripRecordEditor.EditFuel(record.entry)
            is TripRecord.Expense -> TripRecordEditor.EditExpense(record.expense)
            is TripRecord.Pending ->
                if (record.isFuel) {
                    TripRecordEditor.EditPendingFuel(record.record)
                } else {
                    TripRecordEditor.EditPendingExpense(record.record)
                }
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
        if (
            editor !is TripRecordEditor.NewFuel &&
            editor !is TripRecordEditor.EditFuel &&
            editor !is TripRecordEditor.EditPendingFuel
        ) {
            return
        }

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

        when (editor) {
            is TripRecordEditor.EditPendingFuel ->
                changePending(editor.record.copy(fuel = request))

            is TripRecordEditor.EditFuel ->
                mutate("salvar o abastecimento") {
                    repository.updateFuelEntry(tripId, editor.entry.id, request)
                }

            else ->
                mutate(
                    what = "salvar o abastecimento",
                    offline = { newPending(fuel = request) }
                ) {
                    repository.addFuelEntry(tripId, request)
                }
        }
    }

    fun saveExpense(
        category: ExpenseCategory,
        amountText: String,
        description: String
    ) {
        val editor = _uiState.value.editor
        if (
            editor !is TripRecordEditor.NewExpense &&
            editor !is TripRecordEditor.EditExpense &&
            editor !is TripRecordEditor.EditPendingExpense
        ) {
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

        when (editor) {
            is TripRecordEditor.EditPendingExpense ->
                changePending(editor.record.copy(expense = request))

            is TripRecordEditor.EditExpense ->
                mutate("salvar o gasto") {
                    repository.updateExpense(tripId, editor.expense.id, request)
                }

            else ->
                mutate(
                    what = "salvar o gasto",
                    offline = { newPending(expense = request) }
                ) {
                    repository.addExpense(tripId, request)
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

            is TripRecordEditor.EditPendingFuel ->
                removePending(editor.record)

            is TripRecordEditor.EditPendingExpense ->
                removePending(editor.record)

            else -> Unit
        }
    }

    private fun changePending(record: PendingRecord) {
        val store = pendingStore ?: return

        viewModelScope.launch {
            store.replace(record)
            _uiState.value = _uiState.value.copy(editor = null, editorErrorMessage = null)
        }
    }

    private fun removePending(record: PendingRecord) {
        val store = pendingStore ?: return

        viewModelScope.launch {
            store.remove(record.id)
            _uiState.value = _uiState.value.copy(editor = null, editorErrorMessage = null)
        }
    }

    private suspend fun newPending(
        fuel: FuelEntryRequestDTO? = null,
        expense: TripExpenseRequestDTO? = null
    ) {
        pendingStore?.add(
            PendingRecord(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                createdAtMillis = now(),
                fuel = fuel,
                expense = expense
            )
        )
    }

    /**
     * Runs [change] and reloads. With no connection and an [offline] fallback, the
     * record is kept on the device instead of being lost.
     */
    private fun mutate(
        what: String,
        offline: (suspend () -> Unit)? = null,
        change: suspend () -> Unit
    ) {
        val state = _uiState.value
        if (state.isSaving) return

        loadJob?.cancel()
        _uiState.value = state.copy(isSaving = true, editorErrorMessage = null)

        viewModelScope.launch {
            try {
                change()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                if (offline != null && pendingStore != null) {
                    offline()

                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        editor = null,
                        editorErrorMessage = null
                    )
                    _feedback.trySend(
                        Feedback("Sem conexão: guardei o registro e envio assim que a internet voltar.")
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        editorErrorMessage = failureMessage(exception, what)
                    )
                }
                return@launch
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
