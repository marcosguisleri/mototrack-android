@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.maintenance

import br.dev.guisleri.mototrack.data.maintenance.ItemInterval
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceItem
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceLevel
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceStore
import br.dev.guisleri.mototrack.data.maintenance.MotorcycleMaintenance
import br.dev.guisleri.mototrack.data.maintenance.OdometerReading
import br.dev.guisleri.mototrack.data.maintenance.ServiceRecord
import br.dev.guisleri.mototrack.data.model.motorcycle.CreateMotorcycleRequestDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO
import br.dev.guisleri.mototrack.data.remote.motorcycle.MotorcycleApiService
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class MaintenanceViewModelTest {

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
    private val today = LocalDate.parse("2026-10-22")

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsWithNoOdometerAndNothingRegistered() = runTest(dispatcher) {
        val viewModel = viewModel()

        val state = viewModel.uiState.value
        assertEquals("Honda CB 500X", state.motorcycleName)
        assertNull(state.odometerKm)
        assertTrue(state.items.all { it.level == MaintenanceLevel.UNKNOWN })
    }

    @Test
    fun savingTheOdometerRemembersTheTripsAlreadyCounted() = runTest(dispatcher) {
        val store = FakeStore()
        val viewModel = viewModel(store = store, tripKm = 250.0)

        viewModel.openOdometerEditor()
        viewModel.saveOdometer("12000")

        val state = viewModel.uiState.value
        assertEquals(12_000, state.odometerKm)
        assertNull(state.editor)
        assertEquals(250.0, store.current.odometer!!.tripKmAtReading, 0.0)
    }

    @Test
    fun theOdometerFollowsTheTripsDoneAfterTheReading() = runTest(dispatcher) {
        val store = FakeStore(
            MotorcycleMaintenance(odometer = OdometerReading(12_000, 100.0, "2026-10-01"))
        )

        val viewModel = viewModel(store = store, tripKm = 330.0)

        assertEquals(12_230, viewModel.uiState.value.odometerKm)
    }

    @Test
    fun anInvalidOdometerIsRefusedAndKeepsTheDialogOpen() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.openOdometerEditor()
        viewModel.saveOdometer("")

        val state = viewModel.uiState.value
        assertNotNull(state.editorErrorMessage)
        assertNotNull(state.editor)
    }

    @Test
    fun theOdometerNeedsTheTripsToHaveLoaded() = runTest(dispatcher) {
        val viewModel = viewModel(failure = IOException("offline"))

        assertEquals("Não foi possível comunicar com o servidor.", viewModel.uiState.value.errorMessage)

        viewModel.openOdometerEditor()
        viewModel.saveOdometer("12000")

        assertNull(viewModel.uiState.value.odometerKm)
        assertNotNull(viewModel.uiState.value.editorErrorMessage)
    }

    @Test
    fun registeringAServiceCountsFromItsKm() = runTest(dispatcher) {
        val store = FakeStore(
            MotorcycleMaintenance(odometer = OdometerReading(12_000, 0.0, "2026-10-01"))
        )
        val viewModel = viewModel(store = store)

        viewModel.openServiceEditor(MaintenanceItem.OIL)
        viewModel.saveService(MaintenanceItem.OIL, LocalDate.parse("2026-10-10"), "11000", "3000")

        val oil = viewModel.uiState.value.items.first { it.item == MaintenanceItem.OIL }
        assertEquals(2_000, oil.kmLeft)
        assertEquals(MaintenanceLevel.OK, oil.level)
        assertNull(viewModel.uiState.value.editor)
        // The default interval is not saved as a custom one.
        assertTrue(store.current.intervals.isEmpty())
    }

    @Test
    fun aDifferentIntervalIsRemembered() = runTest(dispatcher) {
        val store = FakeStore()
        val viewModel = viewModel(store = store)

        viewModel.openServiceEditor(MaintenanceItem.OIL)
        viewModel.saveService(MaintenanceItem.OIL, today, "", "2500")

        assertEquals(ItemInterval(2_500, 6), store.current.intervals[MaintenanceItem.OIL.key])
        assertEquals(ItemInterval(2_500, 6), viewModel.intervalFor(MaintenanceItem.OIL))
    }

    @Test
    fun aServiceCannotBeInTheFuture() = runTest(dispatcher) {
        val store = FakeStore()
        val viewModel = viewModel(store = store)

        viewModel.openServiceEditor(MaintenanceItem.OIL)
        viewModel.saveService(MaintenanceItem.OIL, today.plusDays(1), "", "3000")

        assertEquals("A data não pode estar no futuro.", viewModel.uiState.value.editorErrorMessage)
        assertTrue(store.current.services.isEmpty())
    }

    @Test
    fun anItemWithoutATimeIntervalNeedsAKmInterval() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.openServiceEditor(MaintenanceItem.CHAIN)
        viewModel.saveService(MaintenanceItem.CHAIN, today, "", "")

        assertEquals(
            "Informe de quantos em quantos km repetir.",
            viewModel.uiState.value.editorErrorMessage
        )
    }

    @Test
    fun clearingAServiceMakesItUnknownAgain() = runTest(dispatcher) {
        val store = FakeStore(
            MotorcycleMaintenance(
                services = mapOf(MaintenanceItem.OIL.key to ServiceRecord(10_000, "2026-10-01"))
            )
        )
        val viewModel = viewModel(store = store)

        viewModel.clearService(MaintenanceItem.OIL)

        val oil = viewModel.uiState.value.items.first { it.item == MaintenanceItem.OIL }
        assertEquals(MaintenanceLevel.UNKNOWN, oil.level)
    }

    private fun viewModel(
        store: FakeStore = FakeStore(),
        tripKm: Double = 0.0,
        failure: Exception? = null
    ) = MaintenanceViewModel(
        motorcycleId = 1,
        motorcycleRepository = MotorcycleRepository(FakeMotorcycleApi(tripKm, failure)),
        store = store,
        today = { today }
    )

    private class FakeStore(initial: MotorcycleMaintenance = MotorcycleMaintenance()) : MaintenanceStore {
        private val state = MutableStateFlow(initial)

        val current: MotorcycleMaintenance get() = state.value

        override fun observe(motorcycleId: Long): Flow<MotorcycleMaintenance> = state

        override suspend fun update(
            motorcycleId: Long,
            change: (MotorcycleMaintenance) -> MotorcycleMaintenance
        ) {
            state.value = change(state.value)
        }
    }

    private class FakeMotorcycleApi(
        private val tripKm: Double,
        private val failure: Exception?
    ) : MotorcycleApiService {

        override suspend fun updateMotorcycle(
            motorcycleId: Long,
            request: br.dev.guisleri.mototrack.data.model.motorcycle.UpdateMotorcycleRequestDTO
        ): br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO =
            throw AssertionError("Unexpected motorcycle update")

        override suspend fun getMotorcycles(): List<MotorcycleResponseDTO> =
            throw AssertionError("Unexpected list request")

        override suspend fun createMotorcycle(
            request: CreateMotorcycleRequestDTO
        ): MotorcycleResponseDTO = throw AssertionError("Unexpected creation")

        override suspend fun deleteMotorcycle(motorcycleId: Long) {
            throw AssertionError("Unexpected deletion")
        }

        override suspend fun getMotorcycleStatistics(
            motorcycleId: Long
        ): MotorcycleStatisticsResponseDTO {
            failure?.let { throw it }

            return MotorcycleStatisticsResponseDTO(
                motorcycle = MotorcycleResponseDTO(
                    id = 1, brand = "Honda", model = "CB 500X", color = "Preta", year = 2024,
                    engineCapacity = 500,
                    owner = MotorcycleOwnerDTO(1, "Rider", "rider@example.com")
                ),
                completedTrips = 0,
                totalCompletedDistanceKm = tripKm,
                averageCompletedDistanceKm = null,
                longestTrip = null,
                lastCompletedTrip = null,
                terrainStatistics = emptyMap()
            )
        }
    }
}
