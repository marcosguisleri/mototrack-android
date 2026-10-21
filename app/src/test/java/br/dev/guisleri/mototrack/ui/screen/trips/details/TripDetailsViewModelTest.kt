@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.trips.details

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.CreateTripRequestDTO
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
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import org.junit.Assert.assertNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

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

    @Test
    fun completingTripWithFutureDateAsksForConfirmationAndThenSendsIt() = runTest(dispatcher) {
        val api = FakeTripApi().apply {
            onUpdateStatus = {
                if (!statusRequests.last().updateTripDateToToday) throw dateConflict()
            }
        }
        val viewModel = TripDetailsViewModel(1, TripRepository(api))

        viewModel.completeTrip()

        val asked = viewModel.uiState.value as TripDetailsUiState.Success
        assertTrue(asked.requiresTripDateUpdateConfirmation)
        assertFalse(asked.isUpdatingStatus)
        assertNull(asked.actionErrorMessage)

        viewModel.confirmTripDateUpdate()

        val state = viewModel.uiState.value as TripDetailsUiState.Success
        assertFalse(state.requiresTripDateUpdateConfirmation)
        assertEquals(
            listOf(
                UpdateTripStatusRequestDTO("COMPLETED", updateTripDateToToday = false),
                UpdateTripStatusRequestDTO("COMPLETED", updateTripDateToToday = true)
            ),
            api.statusRequests
        )
        assertEquals("COMPLETED", state.trip.status)
    }

    @Test
    fun startingTripOnAnotherDateStillConfirmsTheStartNotACompletion() = runTest(dispatcher) {
        val api = FakeTripApi().apply {
            onUpdateStatus = {
                if (!statusRequests.last().updateTripDateToToday) throw dateConflict()
            }
        }
        val viewModel = TripDetailsViewModel(1, TripRepository(api))

        viewModel.startTrip()
        viewModel.confirmTripDateUpdate()

        assertEquals(
            listOf("IN_PROGRESS", "IN_PROGRESS"),
            api.statusRequests.map { it.status }
        )
        assertEquals(
            "IN_PROGRESS",
            (viewModel.uiState.value as TripDetailsUiState.Success).trip.status
        )
    }

    @Test
    fun dismissingTheConfirmationSendsNothingMore() = runTest(dispatcher) {
        val api = FakeTripApi().apply {
            onUpdateStatus = { throw dateConflict() }
        }
        val viewModel = TripDetailsViewModel(1, TripRepository(api))

        viewModel.completeTrip()
        viewModel.dismissTripDateUpdateConfirmation()
        viewModel.confirmTripDateUpdate()

        assertEquals(1, api.statusRequests.size)
        assertFalse(
            (viewModel.uiState.value as TripDetailsUiState.Success)
                .requiresTripDateUpdateConfirmation
        )
    }

    @Test
    fun otherConflictsOnCompletionAreShownAsErrors() = runTest(dispatcher) {
        val api = FakeTripApi().apply {
            onUpdateStatus = { throw HttpException(Response.error<Any>(400, "".toResponseBody())) }
        }
        val viewModel = TripDetailsViewModel(1, TripRepository(api))

        viewModel.completeTrip()

        val state = viewModel.uiState.value as TripDetailsUiState.Success
        assertFalse(state.requiresTripDateUpdateConfirmation)
        assertEquals("Não foi possível alterar o status da viagem.", state.actionErrorMessage)
    }

    @Test
    fun deletingTheTripCallsTheApiAndMarksItDeleted() = runTest(dispatcher) {
        val api = FakeTripApi()
        val viewModel = TripDetailsViewModel(1, TripRepository(api))

        viewModel.deleteTrip()

        val state = viewModel.uiState.value as TripDetailsUiState.Success
        assertEquals(listOf(1L), api.deletedTripIds)
        assertTrue(state.isDeleted)
        assertFalse(state.isDeleting)
    }

    @Test
    fun deleteIsIgnoredWhileAnotherDeleteIsPending() = runTest(dispatcher) {
        val response = CompletableDeferred<Unit>()
        val api = FakeTripApi().apply { onDelete = { response.await() } }
        val viewModel = TripDetailsViewModel(1, TripRepository(api))

        viewModel.deleteTrip()
        viewModel.deleteTrip()

        assertTrue((viewModel.uiState.value as TripDetailsUiState.Success).isDeleting)
        response.complete(Unit)

        assertEquals(listOf(1L), api.deletedTripIds)
    }

    @Test
    fun failedDeleteKeepsTheTripAndShowsTheError() = runTest(dispatcher) {
        val api = FakeTripApi().apply { onDelete = { throw IOException("offline") } }
        val viewModel = TripDetailsViewModel(1, TripRepository(api))

        viewModel.deleteTrip()

        val state = viewModel.uiState.value as TripDetailsUiState.Success
        assertFalse(state.isDeleted)
        assertFalse(state.isDeleting)
        assertEquals("Não foi possível comunicar com o servidor.", state.actionErrorMessage)
    }

    @Test
    fun serverRefusalOnDeleteShowsAGenericError() = runTest(dispatcher) {
        val api = FakeTripApi().apply {
            onDelete = { throw HttpException(Response.error<Any>(404, "".toResponseBody())) }
        }
        val viewModel = TripDetailsViewModel(1, TripRepository(api))

        viewModel.deleteTrip()

        assertEquals(
            "Não foi possível excluir a viagem.",
            (viewModel.uiState.value as TripDetailsUiState.Success).actionErrorMessage
        )
    }

    @Test
    fun statusActionsAreIgnoredWhileDeleting() = runTest(dispatcher) {
        val response = CompletableDeferred<Unit>()
        val api = FakeTripApi().apply { onDelete = { response.await() } }
        val viewModel = TripDetailsViewModel(1, TripRepository(api))

        viewModel.deleteTrip()
        viewModel.startTrip()

        assertTrue(api.statusRequests.isEmpty())
        response.complete(Unit)
    }

    private fun dateConflict() =
        HttpException(Response.error<Any>(409, "".toResponseBody()))

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

        override suspend fun scheduleTrip(
            request: CreateTripRequestDTO
        ): TripResponseDTO =
            throw AssertionError("Unexpected trip creation")

        override suspend fun registerCompletedTrip(
            request: CreateTripRequestDTO
        ): TripResponseDTO =
            throw AssertionError("Unexpected completed trip registration")

        val deletedTripIds = mutableListOf<Long>()
        var onDelete: suspend () -> Unit = {}

        override suspend fun deleteTrip(tripId: Long) {
            onDelete()
            deletedTripIds += tripId
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
