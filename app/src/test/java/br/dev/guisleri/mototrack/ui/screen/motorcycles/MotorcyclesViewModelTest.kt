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

        override suspend fun getMotorcycles() = motorcycles.toList()

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
                completedTrips = 0,
                totalCompletedDistanceKm = 0.0,
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
