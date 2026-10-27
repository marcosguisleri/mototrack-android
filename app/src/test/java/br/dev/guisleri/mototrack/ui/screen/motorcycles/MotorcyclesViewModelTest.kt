@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.motorcycles

import br.dev.guisleri.mototrack.data.local.MotorcyclePreferences
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO
import br.dev.guisleri.mototrack.data.remote.motorcycle.MotorcycleApiService
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import org.junit.Assert.assertFalse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import br.dev.guisleri.mototrack.ui.feedback.Feedback
import br.dev.guisleri.mototrack.ui.feedback.FeedbackKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.junit.Before
import org.junit.Test

class MotorcyclesViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun opensOnTheDefaultMotorcycle() = runTest(dispatcher) {
        val viewModel = viewModel(
            motorcycles = listOf(motorcycle(1), motorcycle(2)),
            preferences = FakePreferences(defaultId = 2)
        )

        val state = viewModel.uiState.value as MotorcyclesUiState.Success
        assertEquals(2L, state.selectedMotorcycleId)
        assertEquals(2L, state.defaultMotorcycleId)
    }

    @Test
    fun withoutDefaultOpensOnTheFirstMotorcycle() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1), motorcycle(2)))

        val state = viewModel.uiState.value as MotorcyclesUiState.Success
        assertEquals(1L, state.selectedMotorcycleId)
        assertNull(state.defaultMotorcycleId)
    }

    @Test
    fun staleDefaultIsIgnored() = runTest(dispatcher) {
        val viewModel = viewModel(
            motorcycles = listOf(motorcycle(1), motorcycle(2)),
            preferences = FakePreferences(defaultId = 99)
        )

        val state = viewModel.uiState.value as MotorcyclesUiState.Success
        assertEquals(1L, state.selectedMotorcycleId)
        assertNull(state.defaultMotorcycleId)
    }

    @Test
    fun settingTheDefaultPersistsItAndKeepsTheSelection() = runTest(dispatcher) {
        val preferences = FakePreferences()
        val viewModel = viewModel(
            motorcycles = listOf(motorcycle(1), motorcycle(2)),
            preferences = preferences
        )

        viewModel.setDefaultMotorcycle(2)

        val state = viewModel.uiState.value as MotorcyclesUiState.Success
        assertEquals(2L, state.defaultMotorcycleId)
        assertEquals(1L, state.selectedMotorcycleId)
        assertEquals(2L, preferences.state.value)
    }

    @Test
    fun unknownMotorcycleCannotBecomeDefault() = runTest(dispatcher) {
        val preferences = FakePreferences()
        val viewModel = viewModel(
            motorcycles = listOf(motorcycle(1)),
            preferences = preferences
        )

        viewModel.setDefaultMotorcycle(99)

        assertNull((viewModel.uiState.value as MotorcyclesUiState.Success).defaultMotorcycleId)
        assertNull(preferences.state.value)
    }

    private fun viewModel(
        motorcycles: List<MotorcycleResponseDTO>,
        preferences: FakePreferences = FakePreferences(),
        api: FakeApi = FakeApi(motorcycles)
    ) = MotorcyclesViewModel(
        motorcycleRepository = MotorcycleRepository(api),
        motorcyclePreferences = preferences
    )

    @Test
    fun deletingAMotorcycleReloadsTheGarage() = runTest(dispatcher) {
        val motorcycles = listOf(motorcycle(1), motorcycle(2))
        val api = FakeApi(motorcycles)
        val viewModel = viewModel(motorcycles, api = api)

        viewModel.deleteMotorcycle(2)

        val state = viewModel.uiState.value as MotorcyclesUiState.Success
        assertEquals(listOf(2L), api.deletedIds)
        assertEquals(listOf(1L), state.motorcycles.map { it.id })
        assertFalse(state.isDeleting)
    }

    @Test
    fun deletingTheSelectedMotorcycleMovesToAnother() = runTest(dispatcher) {
        val motorcycles = listOf(motorcycle(1), motorcycle(2))
        val viewModel = viewModel(motorcycles, api = FakeApi(motorcycles))
        viewModel.selectMotorcycle(2)

        viewModel.deleteMotorcycle(2)

        assertEquals(
            1L,
            (viewModel.uiState.value as MotorcyclesUiState.Success).selectedMotorcycleId
        )
    }

    @Test
    fun deletingTheLastMotorcycleLeavesTheGarageEmpty() = runTest(dispatcher) {
        val motorcycles = listOf(motorcycle(1))
        val viewModel = viewModel(motorcycles, api = FakeApi(motorcycles))

        viewModel.deleteMotorcycle(1)

        assertEquals(MotorcyclesUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun serverConflictMeansTheMotorcycleStillHasTrips() = runTest(dispatcher) {
        val motorcycles = listOf(motorcycle(1), motorcycle(2))
        val api = FakeApi(motorcycles).apply {
            deleteFailure = HttpException(Response.error<Any>(409, "".toResponseBody()))
        }
        val viewModel = viewModel(motorcycles, api = api)

        viewModel.deleteMotorcycle(2)

        val state = viewModel.uiState.value as MotorcyclesUiState.Success
        assertEquals(MotorcycleDeleteIssue.HasTrips, state.deleteIssue)
        assertEquals(2, state.motorcycles.size)
        assertFalse(state.isDeleting)
    }

    @Test
    fun otherDeleteFailuresAreReportedAsFailures() = runTest(dispatcher) {
        val motorcycles = listOf(motorcycle(1))
        val api = FakeApi(motorcycles).apply { deleteFailure = IOException("offline") }
        val viewModel = viewModel(motorcycles, api = api)

        viewModel.deleteMotorcycle(1)

        val issue = (viewModel.uiState.value as MotorcyclesUiState.Success).deleteIssue
        assertEquals(
            MotorcycleDeleteIssue.Failed("Não foi possível comunicar com o servidor."),
            issue
        )
    }

    @Test
    fun dismissingTheIssueClearsIt() = runTest(dispatcher) {
        val motorcycles = listOf(motorcycle(1))
        val api = FakeApi(motorcycles).apply {
            deleteFailure = HttpException(Response.error<Any>(409, "".toResponseBody()))
        }
        val viewModel = viewModel(motorcycles, api = api)
        viewModel.deleteMotorcycle(1)

        viewModel.dismissDeleteIssue()

        assertNull((viewModel.uiState.value as MotorcyclesUiState.Success).deleteIssue)
    }

    @Test
    fun deletingAMotorcycleConfirmsItByName() = runTest(dispatcher) {
        val motorcycles = listOf(motorcycle(1), motorcycle(2))
        val viewModel = viewModel(motorcycles, api = FakeApi(motorcycles))

        viewModel.deleteMotorcycle(2)

        assertEquals(
            Feedback("Moto excluída: Honda CB 500X", FeedbackKind.SUCCESS),
            viewModel.feedback.first()
        )
    }

    @Test
    fun failedDeleteSendsNoConfirmation() = runTest(dispatcher) {
        val motorcycles = listOf(motorcycle(1))
        val api = FakeApi(motorcycles).apply { deleteFailure = IOException("offline") }
        val viewModel = viewModel(motorcycles, api = api)

        viewModel.deleteMotorcycle(1)
        viewModel.setDefaultMotorcycle(1)

        // The only message is the one about the default, so nothing was sent for the delete.
        assertEquals(
            listOf(Feedback("Moto padrão: Honda CB 500X")),
            viewModel.feedback.take(1).toList()
        )
    }

    @Test
    fun settingTheDefaultConfirmsIt() = runTest(dispatcher) {
        val viewModel = viewModel(listOf(motorcycle(1), motorcycle(2)))

        viewModel.setDefaultMotorcycle(2)

        assertEquals(
            Feedback("Moto padrão: Honda CB 500X"),
            viewModel.feedback.first()
        )
    }

    @Test
    fun refreshBringsInTheTripsCompletedSinceWithoutReloadingTheScreen() = runTest(dispatcher) {
        val api = FakeApi(listOf(motorcycle(1), motorcycle(2)))
        val viewModel = viewModel(listOf(motorcycle(1), motorcycle(2)), api = api)
        viewModel.selectMotorcycle(2)

        // A trip is completed elsewhere.
        api.totalKm = 120.0
        api.completedTrips = 1

        viewModel.refresh()

        val state = viewModel.uiState.value as MotorcyclesUiState.Success
        val statistics = (state.statisticsUiState as MotorcycleStatisticsUiState.Success).statistics
        assertEquals(1L, statistics.completedTrips)
        assertEquals(120.0, statistics.totalCompletedDistanceKm, 0.0)
        assertEquals(2L, state.selectedMotorcycleId)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun refreshFailureKeepsWhatWasOnScreenAndSaysSo() = runTest(dispatcher) {
        val api = FakeApi(listOf(motorcycle(1)))
        val viewModel = viewModel(listOf(motorcycle(1)), api = api)

        api.listFailure = IOException("offline")
        viewModel.refresh()

        val state = viewModel.uiState.value as MotorcyclesUiState.Success
        assertFalse(state.isRefreshing)
        assertEquals(1, state.motorcycles.size)
        assertEquals(
            Feedback("Sem conexão: não foi possível atualizar suas motos.", FeedbackKind.ERROR),
            viewModel.feedback.first()
        )
    }

    @Test
    fun refreshDropsAMotorcycleRemovedMeanwhile() = runTest(dispatcher) {
        val api = FakeApi(listOf(motorcycle(1), motorcycle(2)))
        val viewModel = viewModel(listOf(motorcycle(1), motorcycle(2)), api = api)
        viewModel.selectMotorcycle(2)

        api.deleteMotorcycle(2)
        viewModel.refresh()

        val state = viewModel.uiState.value as MotorcyclesUiState.Success
        assertEquals(listOf(1L), state.motorcycles.map { it.id })
        assertEquals(1L, state.selectedMotorcycleId)
    }

    @Test
    fun refreshLoadsFromScratchWhenThereWasAnError() = runTest(dispatcher) {
        val api = FakeApi(listOf(motorcycle(1)))
        api.listFailure = IOException("offline")
        val viewModel = viewModel(listOf(motorcycle(1)), api = api)
        assertEquals(true, viewModel.uiState.value is MotorcyclesUiState.Error)

        api.listFailure = null
        viewModel.refresh()

        assertEquals(true, viewModel.uiState.value is MotorcyclesUiState.Success)
    }

    private class FakePreferences(defaultId: Long? = null) : MotorcyclePreferences {
        val state = MutableStateFlow(defaultId)

        override val defaultMotorcycleId: Flow<Long?> = state

        override suspend fun setDefaultMotorcycleId(motorcycleId: Long) {
            state.value = motorcycleId
        }
    }

    private class FakeApi(
        motorcycles: List<MotorcycleResponseDTO>
    ) : MotorcycleApiService {

        private val motorcycles = motorcycles.toMutableList()
        val deletedIds = mutableListOf<Long>()
        var deleteFailure: Exception? = null
        var listFailure: Exception? = null
        var totalKm = 0.0
        var completedTrips = 0L
        var listCalls = 0

        override suspend fun updateMotorcycle(
            motorcycleId: Long,
            request: br.dev.guisleri.mototrack.data.model.motorcycle.UpdateMotorcycleRequestDTO
        ): br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO =
            throw AssertionError("Unexpected motorcycle update")

        override suspend fun getMotorcycles(): List<MotorcycleResponseDTO> {
            listCalls++
            listFailure?.let { throw it }
            return motorcycles.toList()
        }

        override suspend fun deleteMotorcycle(motorcycleId: Long) {
            deleteFailure?.let { throw it }
            deletedIds += motorcycleId
            motorcycles.removeAll { it.id == motorcycleId }
        }

        override suspend fun createMotorcycle(
            request: br.dev.guisleri.mototrack.data.model.motorcycle.CreateMotorcycleRequestDTO
        ): MotorcycleResponseDTO =
            throw AssertionError("Unexpected motorcycle creation")

        override suspend fun getMotorcycleStatistics(
            motorcycleId: Long
        ): MotorcycleStatisticsResponseDTO =
            MotorcycleStatisticsResponseDTO(
                motorcycle = motorcycles.first { it.id == motorcycleId },
                completedTrips = completedTrips,
                totalCompletedDistanceKm = totalKm,
                averageCompletedDistanceKm = null,
                longestTrip = null,
                lastCompletedTrip = null,
                terrainStatistics = emptyMap()
            )
    }

    private companion object {
        fun motorcycle(id: Long) = MotorcycleResponseDTO(
            id = id,
            brand = "Honda",
            model = "CB 500X",
            color = "Preta",
            year = 2024,
            engineCapacity = 500,
            owner = MotorcycleOwnerDTO(1, "Rider", "rider@example.com")
        )
    }
}
