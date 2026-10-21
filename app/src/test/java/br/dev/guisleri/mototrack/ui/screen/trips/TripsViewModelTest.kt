@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.trips

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.data.remote.trip.TripApiService
import br.dev.guisleri.mototrack.data.repository.TripRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class TripsViewModelTest {

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
    fun refreshFailureKeepsTripsAndExposesMessage() = runTest(dispatcher) {
        val initialTrips = listOf(trip())
        val api = FakeTripApi().apply { onGetTrips = { initialTrips } }
        val viewModel = TripsViewModel(TripRepository(api))

        api.onGetTrips = { throw IOException("offline") }
        viewModel.refreshTrips()

        val state = viewModel.uiState.value as TripsUiState.Success
        assertEquals(initialTrips, state.trips)
        assertEquals("Não foi possível comunicar com o servidor.", state.refreshErrorMessage)
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun overlappingRefreshRequestsAreIgnored() = runTest(dispatcher) {
        val response = CompletableDeferred<List<TripResponseDTO>>()
        val api = FakeTripApi().apply { onGetTrips = { listOf(trip()) } }
        val viewModel = TripsViewModel(TripRepository(api))
        api.getTripsCalls = 0
        api.onGetTrips = { response.await() }

        viewModel.refreshTrips()
        viewModel.refreshTrips()

        assertEquals(1, api.getTripsCalls)
        assertTrue(viewModel.isRefreshing.value)
        response.complete(listOf(trip(status = "COMPLETED")))

        val state = viewModel.uiState.value as TripsUiState.Success
        assertEquals("COMPLETED", state.trips.single().status)
        assertFalse(viewModel.isRefreshing.value)
    }

    private fun trip(status: String = "PLANNED") = TripResponseDTO(
        id = 1,
        origin = "São Paulo",
        destination = "Santos",
        distanceKm = 70.0,
        status = status,
        terrain = "ASPHALT",
        tripDate = "2026-10-19",
        motorcycle = MotorcycleResponseDTO(
            id = 1,
            brand = "Honda",
            model = "CB 500X",
            color = "Preta",
            year = 2024,
            engineCapacity = 500,
            owner = MotorcycleOwnerDTO(1, "Rider", "rider@example.com")
        )
    )

    private class FakeTripApi : TripApiService {
        var getTripsCalls = 0
        var onGetTrips: suspend () -> List<TripResponseDTO> = { emptyList() }

        override suspend fun getTrips(): List<TripResponseDTO> {
            getTripsCalls++
            return onGetTrips()
        }

        override suspend fun getTripById(tripId: Long): TripResponseDTO =
            throw AssertionError("Unexpected detail request")

        override suspend fun scheduleTrip(
            request: br.dev.guisleri.mototrack.data.model.trip.CreateTripRequestDTO
        ): TripResponseDTO =
            throw AssertionError("Unexpected trip creation")

        override suspend fun registerCompletedTrip(
            request: br.dev.guisleri.mototrack.data.model.trip.CreateTripRequestDTO
        ): TripResponseDTO =
            throw AssertionError("Unexpected completed trip registration")

        override suspend fun updateTripStatus(
            tripId: Long,
            request: br.dev.guisleri.mototrack.data.model.trip.UpdateTripStatusRequestDTO
        ) {
            throw AssertionError("Unexpected status update")
        }
    }
}
