@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.trips.details

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.UpdateTripStatusRequestDTO
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

class TripDetailsViewModelTest {

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
    fun acceptedStatusUpdatePublishesRefreshedTrip() = runTest(dispatcher) {
        val api = FakeTripApi()
        val viewModel = TripDetailsViewModel(1, TripRepository(api))

        viewModel.startTrip()

        val state = viewModel.uiState.value as TripDetailsUiState.Success
        assertEquals("IN_PROGRESS", state.trip.status)
        assertFalse(state.isUpdatingStatus)
        assertEquals(2, api.getTripCalls)
        assertEquals(listOf(UpdateTripStatusRequestDTO("IN_PROGRESS")), api.statusRequests)
    }

    @Test
    fun duplicateStatusActionsAreIgnoredWhileRequestIsPending() = runTest(dispatcher) {
        val response = CompletableDeferred<Unit>()
        val api = FakeTripApi().apply { onUpdateStatus = { response.await() } }
        val viewModel = TripDetailsViewModel(1, TripRepository(api))

        viewModel.startTrip()
        viewModel.completeTrip()

        assertEquals(1, api.statusRequests.size)
        assertTrue((viewModel.uiState.value as TripDetailsUiState.Success).isUpdatingStatus)
        response.complete(Unit)

        val state = viewModel.uiState.value as TripDetailsUiState.Success
        assertEquals("IN_PROGRESS", state.trip.status)
        assertFalse(state.isUpdatingStatus)
    }

    private class FakeTripApi : TripApiService {
        var getTripCalls = 0
        val statusRequests = mutableListOf<UpdateTripStatusRequestDTO>()
        var onUpdateStatus: suspend () -> Unit = {}
        private var status = "PLANNED"

        override suspend fun getTrips(): List<TripResponseDTO> =
            throw AssertionError("Unexpected list request")

        override suspend fun getTripById(tripId: Long): TripResponseDTO {
            getTripCalls++
            return trip(status)
        }

        override suspend fun updateTripStatus(
            tripId: Long,
            request: UpdateTripStatusRequestDTO
        ) {
            statusRequests += request
            onUpdateStatus()
            status = request.status
        }
    }

    private companion object {
        fun trip(status: String = "PLANNED") = TripResponseDTO(
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
    }
}
