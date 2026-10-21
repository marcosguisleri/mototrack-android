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
        preferences: FakePreferences = FakePreferences()
    ) = MotorcyclesViewModel(
        motorcycleRepository = MotorcycleRepository(FakeApi(motorcycles)),
        motorcyclePreferences = preferences
    )

    private class FakePreferences(defaultId: Long? = null) : MotorcyclePreferences {
        val state = MutableStateFlow(defaultId)

        override val defaultMotorcycleId: Flow<Long?> = state

        override suspend fun setDefaultMotorcycleId(motorcycleId: Long) {
            state.value = motorcycleId
        }
    }

    private class FakeApi(
        private val motorcycles: List<MotorcycleResponseDTO>
    ) : MotorcycleApiService {

        override suspend fun getMotorcycles() = motorcycles

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
