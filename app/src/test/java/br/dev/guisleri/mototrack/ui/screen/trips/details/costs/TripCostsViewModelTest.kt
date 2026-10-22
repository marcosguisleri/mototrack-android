@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.trips.details.costs

import br.dev.guisleri.mototrack.data.model.trip.ExpenseCategoryCostDTO
import br.dev.guisleri.mototrack.data.model.trip.FuelCostDTO
import br.dev.guisleri.mototrack.data.model.trip.FuelEntryRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.FuelEntryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripCostSummaryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseResponseDTO
import br.dev.guisleri.mototrack.data.remote.trip.TripCostApiService
import br.dev.guisleri.mototrack.data.repository.TripCostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class TripCostsViewModelTest {

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsSummaryAndRecordsTogether() = runTest(dispatcher) {
        val api = FakeCostApi().apply {
            fuel += fuelEntry(1, 10.0, 62.0, "2026-10-20T10:00:00Z")
            expenses += expense(2, "TOLL", 8.9, "2026-10-20T12:00:00Z")
        }

        val state = TripCostsViewModel(1, TripCostRepository(api)).uiState.value

        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(70.9, state.summary!!.totalCost!!, 0.001)
        // The latest first, whatever the kind.
        assertEquals(listOf(2L, 1L), state.records.map { it.id })
    }

    @Test
    fun failedFirstLoadShowsAnErrorAndRetryRecovers() = runTest(dispatcher) {
        val api = FakeCostApi().apply { failure = IOException("offline") }
        val viewModel = TripCostsViewModel(1, TripCostRepository(api))

        assertEquals(
            "Não foi possível comunicar com o servidor.",
            viewModel.uiState.value.errorMessage
        )

        api.failure = null
        viewModel.retry()

        assertNull(viewModel.uiState.value.errorMessage)
        assertNotNull(viewModel.uiState.value.summary)
    }

    @Test
    fun failedRefreshKeepsWhatWasOnScreen() = runTest(dispatcher) {
        val api = FakeCostApi().apply { fuel += fuelEntry(1, 10.0, 62.0) }
        val viewModel = TripCostsViewModel(1, TripCostRepository(api))

        api.failure = IOException("offline")
        viewModel.refresh()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNotNull(state.refreshErrorMessage)
        assertEquals(1, state.fuelEntries.size)
    }

    @Test
    fun savingFuelSendsTheEntryReloadsAndClosesTheEditor() = runTest(dispatcher) {
        val api = FakeCostApi()
        val viewModel = TripCostsViewModel(1, TripCostRepository(api))

        viewModel.addFuel()
        viewModel.saveFuel("12,5", "77,40")

        val state = viewModel.uiState.value
        assertEquals(listOf(FuelEntryRequestDTO(12.5, 77.4)), api.fuelRequests)
        assertNull(state.editor)
        assertFalse(state.isSaving)
        assertEquals(1, state.fuelEntries.size)
        assertEquals(77.4, state.summary!!.fuel.totalCost!!, 0.001)
    }

    @Test
    fun invalidFuelNeverReachesTheServer() = runTest(dispatcher) {
        val api = FakeCostApi()
        val viewModel = TripCostsViewModel(1, TripCostRepository(api))

        viewModel.addFuel()
        viewModel.saveFuel("0", "50")
        assertEquals("Informe os litros abastecidos.", viewModel.uiState.value.editorErrorMessage)

        viewModel.saveFuel("10", "")
        assertEquals("Informe o valor pago.", viewModel.uiState.value.editorErrorMessage)

        assertTrue(api.fuelRequests.isEmpty())
        assertNotNull(viewModel.uiState.value.editor)
    }

    @Test
    fun anExemptTollCanBeZeroButOtherExpensesCannot() = runTest(dispatcher) {
        val api = FakeCostApi()
        val viewModel = TripCostsViewModel(1, TripCostRepository(api))

        viewModel.addExpense(ExpenseCategory.FOOD)
        viewModel.saveExpense(ExpenseCategory.FOOD, "0", "")
        assertEquals("O valor deve ser maior que zero.", viewModel.uiState.value.editorErrorMessage)
        assertTrue(api.expenseRequests.isEmpty())

        viewModel.addExpense(ExpenseCategory.TOLL)
        viewModel.saveExpense(ExpenseCategory.TOLL, "0", "  ")

        assertEquals(
            listOf(TripExpenseRequestDTO("TOLL", 0.0, null)),
            api.expenseRequests
        )
        assertNull(viewModel.uiState.value.editor)
    }

    @Test
    fun editingARecordUpdatesIt() = runTest(dispatcher) {
        val api = FakeCostApi().apply { expenses += expense(5, "FOOD", 30.0) }
        val viewModel = TripCostsViewModel(1, TripCostRepository(api))

        viewModel.editRecord(viewModel.uiState.value.records.single())
        viewModel.saveExpense(ExpenseCategory.FOOD, "35,5", "Almoço")

        assertEquals(listOf(5L), api.updatedExpenseIds)
        assertEquals(35.5, viewModel.uiState.value.expenses.single().amount, 0.001)
        assertEquals("Almoço", viewModel.uiState.value.expenses.single().description)
    }

    @Test
    fun deletingARecordRemovesItAndReloads() = runTest(dispatcher) {
        val api = FakeCostApi().apply { fuel += fuelEntry(3, 10.0, 60.0) }
        val viewModel = TripCostsViewModel(1, TripCostRepository(api))

        viewModel.editRecord(viewModel.uiState.value.records.single())
        viewModel.deleteEditedRecord()

        val state = viewModel.uiState.value
        assertTrue(state.fuelEntries.isEmpty())
        assertNull(state.editor)
        assertNull(state.summary!!.totalCost)
    }

    @Test
    fun serverMessageIsShownAndTheEditorStaysOpen() = runTest(dispatcher) {
        val api = FakeCostApi().apply {
            onAddFuel = {
                throw HttpException(
                    Response.error<Any>(
                        400,
                        """{"status":400,"error":"Bad Request","message":"Só é possível registrar abastecimentos e gastos em viagens iniciadas ou concluídas."}"""
                            .toResponseBody()
                    )
                )
            }
        }
        val viewModel = TripCostsViewModel(1, TripCostRepository(api))

        viewModel.addFuel()
        viewModel.saveFuel("10", "60")

        val state = viewModel.uiState.value
        assertNotNull(state.editor)
        assertFalse(state.isSaving)
        assertTrue(state.editorErrorMessage!!.startsWith("Só é possível registrar"))
    }

    @Test
    fun savingIsIgnoredWhileAnotherSaveIsRunning() = runTest(dispatcher) {
        val response = kotlinx.coroutines.CompletableDeferred<Unit>()
        val api = FakeCostApi().apply { onAddFuel = { response.await() } }
        val viewModel = TripCostsViewModel(1, TripCostRepository(api))

        viewModel.addFuel()
        viewModel.saveFuel("10", "60")
        viewModel.saveFuel("10", "60")

        assertEquals(1, api.fuelRequests.size)
        response.complete(Unit)
    }

    private class FakeCostApi : TripCostApiService {
        val fuel = mutableListOf<FuelEntryResponseDTO>()
        val expenses = mutableListOf<TripExpenseResponseDTO>()
        val fuelRequests = mutableListOf<FuelEntryRequestDTO>()
        val expenseRequests = mutableListOf<TripExpenseRequestDTO>()
        val updatedExpenseIds = mutableListOf<Long>()
        var failure: Exception? = null
        var onAddFuel: suspend () -> Unit = {}
        private var nextId = 100L

        override suspend fun getCostSummary(tripId: Long): TripCostSummaryResponseDTO {
            failure?.let { throw it }

            val liters = fuel.sumOf { it.liters }
            val fuelTotal = fuel.sumOf { it.totalCost }
            val byCategory = expenses.groupBy { it.category }.map { (category, items) ->
                ExpenseCategoryCostDTO(category, items.size.toLong(), items.sumOf { it.amount })
            }
            val empty = fuel.isEmpty() && expenses.isEmpty()
            val total = fuelTotal + expenses.sumOf { it.amount }

            return TripCostSummaryResponseDTO(
                fuel = FuelCostDTO(
                    entries = fuel.size.toLong(),
                    liters = liters.takeIf { fuel.isNotEmpty() },
                    totalCost = fuelTotal.takeIf { fuel.isNotEmpty() }
                ),
                expenses = byCategory,
                totalCost = total.takeUnless { empty },
                costPerKm = total.takeUnless { empty }
            )
        }

        override suspend fun getFuelEntries(tripId: Long): List<FuelEntryResponseDTO> {
            failure?.let { throw it }
            return fuel.toList()
        }

        override suspend fun addFuelEntry(
            tripId: Long,
            request: FuelEntryRequestDTO
        ): FuelEntryResponseDTO {
            fuelRequests += request
            onAddFuel()
            return fuelEntry(nextId++, request.liters, request.totalCost).also { fuel += it }
        }

        override suspend fun updateFuelEntry(
            tripId: Long,
            fuelEntryId: Long,
            request: FuelEntryRequestDTO
        ): FuelEntryResponseDTO = throw AssertionError("Unexpected fuel update")

        override suspend fun deleteFuelEntry(tripId: Long, fuelEntryId: Long) {
            fuel.removeAll { it.id == fuelEntryId }
        }

        override suspend fun getExpenses(tripId: Long): List<TripExpenseResponseDTO> {
            failure?.let { throw it }
            return expenses.toList()
        }

        override suspend fun addExpense(
            tripId: Long,
            request: TripExpenseRequestDTO
        ): TripExpenseResponseDTO {
            expenseRequests += request
            return expense(nextId++, request.category, request.amount, description = request.description)
                .also { expenses += it }
        }

        override suspend fun updateExpense(
            tripId: Long,
            expenseId: Long,
            request: TripExpenseRequestDTO
        ): TripExpenseResponseDTO {
            updatedExpenseIds += expenseId
            val index = expenses.indexOfFirst { it.id == expenseId }
            return expenses[index].copy(
                category = request.category,
                amount = request.amount,
                description = request.description
            ).also { expenses[index] = it }
        }

        override suspend fun deleteExpense(tripId: Long, expenseId: Long) {
            expenses.removeAll { it.id == expenseId }
        }
    }

    private companion object {
        fun fuelEntry(
            id: Long,
            liters: Double,
            totalCost: Double,
            createdAt: String = "2026-10-20T10:00:00Z"
        ) = FuelEntryResponseDTO(id, liters, totalCost, totalCost / liters, createdAt)

        fun expense(
            id: Long,
            category: String,
            amount: Double,
            createdAt: String = "2026-10-20T10:00:00Z",
            description: String? = null
        ) = TripExpenseResponseDTO(id, category, amount, description, createdAt)
    }
}
