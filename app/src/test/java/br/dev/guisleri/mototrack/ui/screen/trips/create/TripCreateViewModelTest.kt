@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.trips.create

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.CreateTripRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.UpdateTripRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.UpdateTripStatusRequestDTO
import br.dev.guisleri.mototrack.data.local.MotorcyclePreferences
import br.dev.guisleri.mototrack.data.remote.motorcycle.MotorcycleApiService
import br.dev.guisleri.mototrack.data.place.ARARAS
import br.dev.guisleri.mototrack.data.place.City
import br.dev.guisleri.mototrack.data.place.CityCatalog
import br.dev.guisleri.mototrack.data.place.FakeGeocodingApi
import br.dev.guisleri.mototrack.data.place.FakeIbgeApi
import br.dev.guisleri.mototrack.data.place.FakeOsrmApi
import br.dev.guisleri.mototrack.data.place.LEME
import br.dev.guisleri.mototrack.data.place.PlaceServices
import br.dev.guisleri.mototrack.data.place.RouteDistanceService
import br.dev.guisleri.mototrack.data.remote.trip.TripApiService
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository
import br.dev.guisleri.mototrack.data.repository.TripRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.time.LocalDate

class TripCreateViewModelTest {

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
    fun singleMotorcycleIsPreselected() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(7)))

        assertEquals(7L, viewModel.uiState.value.selectedMotorcycleId)
    }

    @Test
    fun initialModeIsAppliedToTheForm() = runTest(dispatcher) {
        val viewModel = TripCreateViewModel(
            tripRepository = TripRepository(FakeTripApi()),
            motorcycleRepository = MotorcycleRepository(
                FakeMotorcycleApi(listOf(motorcycle(1)))
            ),
            motorcyclePreferences = FakeMotorcyclePreferences(),
            initialMode = TripCreateMode.COMPLETED
        )

        assertEquals(TripCreateMode.COMPLETED, viewModel.uiState.value.mode)
    }

    @Test
    fun multipleMotorcyclesRequireExplicitSelection() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1), motorcycle(2)))

        assertNull(viewModel.uiState.value.selectedMotorcycleId)
    }

    @Test
    fun submitWithEmptyFormExposesFieldErrorsAndDoesNotCallApi() = runTest(dispatcher) {
        val tripApi = FakeTripApi()
        val viewModel = viewModel(
            tripApi = tripApi,
            motorcycles = listOf(motorcycle(1), motorcycle(2))
        )

        viewModel.submit()

        val state = viewModel.uiState.value
        assertNotNull(state.originError)
        assertNotNull(state.destinationError)
        assertNotNull(state.distanceError)
        assertNotNull(state.motorcycleError)
        assertFalse(state.isSubmitting)
        assertTrue(tripApi.scheduled.isEmpty())
        assertTrue(tripApi.completed.isEmpty())
    }

    @Test
    fun editingAFieldClearsItsError() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)))

        viewModel.submit()
        assertNotNull(viewModel.uiState.value.originError)

        viewModel.onOriginChange("Curitiba")

        assertNull(viewModel.uiState.value.originError)
    }

    @Test
    fun plannedSubmitCallsScheduleEndpoint() = runTest(dispatcher) {
        val tripApi = FakeTripApi()
        val viewModel = viewModel(tripApi = tripApi, motorcycles = listOf(motorcycle(3)))
        fillValidForm(viewModel)

        viewModel.submit()

        assertEquals(1, tripApi.scheduled.size)
        assertTrue(tripApi.completed.isEmpty())

        val request = tripApi.scheduled.single()
        assertEquals("Curitiba", request.origin)
        assertEquals("Florianópolis", request.destination)
        assertEquals(412.5, request.distanceKm, 0.0)
        assertEquals("MIXED", request.terrain)
        assertEquals(3L, request.motorcycleId)
        assertTrue(viewModel.uiState.value.isCreated)
    }

    @Test
    fun completedSubmitCallsCompletedEndpoint() = runTest(dispatcher) {
        val tripApi = FakeTripApi()
        val viewModel = viewModel(tripApi = tripApi, motorcycles = listOf(motorcycle(3)))
        fillValidForm(viewModel)
        viewModel.onModeSelected(TripCreateMode.COMPLETED)

        viewModel.submit()

        assertEquals(1, tripApi.completed.size)
        assertTrue(tripApi.scheduled.isEmpty())
        assertTrue(viewModel.uiState.value.isCreated)
    }

    @Test
    fun switchingModeResetsDateThatIsInvalidForTheNewMode() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)))
        val today = LocalDate.now()

        viewModel.onTripDateSelected(today.plusDays(10))
        viewModel.onModeSelected(TripCreateMode.COMPLETED)
        assertEquals(today, viewModel.uiState.value.tripDate)

        viewModel.onTripDateSelected(today.minusDays(10))
        viewModel.onModeSelected(TripCreateMode.PLANNED)
        assertEquals(today, viewModel.uiState.value.tripDate)
    }

    @Test
    fun distanceInputKeepsOnlyOneSeparatorAndOneDecimal() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)))

        viewModel.onDistanceChange("12a3.45.6")

        assertEquals("123,4", viewModel.uiState.value.distance)
    }

    @Test
    fun networkFailureKeepsFormAndExposesMessage() = runTest(dispatcher) {
        val tripApi = FakeTripApi().apply {
            onSchedule = { throw IOException("offline") }
        }
        val viewModel = viewModel(tripApi = tripApi, motorcycles = listOf(motorcycle(1)))
        fillValidForm(viewModel)

        viewModel.submit()

        val state = viewModel.uiState.value
        assertFalse(state.isCreated)
        assertFalse(state.isSubmitting)
        assertEquals("  Curitiba ", state.origin)
        assertEquals("Não foi possível comunicar com o servidor.", state.submitErrorMessage)
    }

    private fun fillValidForm(viewModel: TripCreateViewModel) {
        viewModel.onOriginChange("  Curitiba ")
        viewModel.onDestinationChange("Florianópolis")
        viewModel.onDistanceChange("412,5")
        viewModel.onTerrainSelected(TripCreateTerrain.MIXED)
        viewModel.onMotorcycleSelected(
            (viewModel.uiState.value.motorcycles as TripCreateMotorcyclesState.Loaded)
                .motorcycles
                .first()
                .id
        )
    }

    private fun historyTrip(
        id: Long,
        origin: String,
        destination: String,
        distanceKm: Double,
        date: String,
        motorcycleId: Long,
        status: String = "COMPLETED"
    ) = TripResponseDTO(
        id = id,
        origin = origin,
        destination = destination,
        distanceKm = distanceKm,
        status = status,
        terrain = "ASPHALT",
        tripDate = date,
        motorcycle = motorcycle(motorcycleId)
    )

    @Test
    fun swapRouteExchangesOriginAndDestination() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)))
        viewModel.onOriginChange("Curitiba")
        viewModel.onDestinationChange("Morretes")

        viewModel.swapRoute()

        assertEquals("Morretes", viewModel.uiState.value.origin)
        assertEquals("Curitiba", viewModel.uiState.value.destination)
    }

    @Test
    fun knownRouteSuggestsDistanceUntilOneIsTyped() = runTest(dispatcher) {
        val tripApi = FakeTripApi().apply {
            history = listOf(
                historyTrip(1, "Campinas", "Santos", 170.0, "2026-09-01", 1)
            )
        }
        val viewModel = viewModel(tripApi = tripApi, motorcycles = listOf(motorcycle(1)))

        viewModel.onOriginChange("campinas ")
        viewModel.onDestinationChange("SANTOS")
        assertEquals(170.0, viewModel.uiState.value.distanceSuggestion)

        viewModel.applyDistanceSuggestion()
        assertEquals("170", viewModel.uiState.value.distance)
        assertNull(viewModel.uiState.value.distanceSuggestion)
    }

    @Test
    fun reverseRouteAlsoSuggestsDistance() = runTest(dispatcher) {
        val tripApi = FakeTripApi().apply {
            history = listOf(
                historyTrip(1, "Campinas", "Santos", 170.5, "2026-09-01", 1)
            )
        }
        val viewModel = viewModel(tripApi = tripApi, motorcycles = listOf(motorcycle(1)))

        viewModel.onOriginChange("Santos")
        viewModel.onDestinationChange("Campinas")
        viewModel.applyDistanceSuggestion()

        assertEquals("170,5", viewModel.uiState.value.distance)
    }

    @Test
    fun recentPlacesAreRankedByFrequency() = runTest(dispatcher) {
        val tripApi = FakeTripApi().apply {
            history = listOf(
                historyTrip(1, "Campinas", "Santos", 170.0, "2026-09-01", 1),
                historyTrip(2, "Campinas", "Leme", 90.0, "2026-09-10", 1),
                historyTrip(3, "Leme", "Santos", 250.0, "2026-09-20", 1)
            )
        }
        val viewModel = viewModel(tripApi = tripApi, motorcycles = listOf(motorcycle(1)))

        assertEquals(
            listOf("Campinas", "Santos", "Leme").sorted(),
            viewModel.uiState.value.recentPlaces.sorted()
        )
        assertEquals(3, viewModel.uiState.value.recentPlaces.size)
    }

    @Test
    fun motorcycleOfTheLatestTripIsPreselected() = runTest(dispatcher) {
        val tripApi = FakeTripApi().apply {
            history = listOf(
                historyTrip(1, "A", "B", 10.0, "2026-08-01", 1),
                historyTrip(2, "C", "D", 10.0, "2026-09-01", 2)
            )
        }
        val viewModel = viewModel(
            tripApi = tripApi,
            motorcycles = listOf(motorcycle(1), motorcycle(2))
        )

        assertEquals(2L, viewModel.uiState.value.selectedMotorcycleId)
    }

    private fun viewModel(
        tripApi: FakeTripApi = FakeTripApi(),
        motorcycles: List<MotorcycleResponseDTO>,
        preferences: FakeMotorcyclePreferences = FakeMotorcyclePreferences(),
        requestedMotorcycleId: Long? = null,
        editingTripId: Long? = null,
        places: PlaceServices? = null
    ) = TripCreateViewModel(
        tripRepository = TripRepository(tripApi),
        motorcycleRepository = MotorcycleRepository(FakeMotorcycleApi(motorcycles)),
        motorcyclePreferences = preferences,
        requestedMotorcycleId = requestedMotorcycleId,
        editingTripId = editingTripId,
        places = places
    )

    private fun places(
        osrm: FakeOsrmApi = FakeOsrmApi(),
        geocoding: FakeGeocodingApi = FakeGeocodingApi()
    ): PlaceServices {
        val ibge = FakeIbgeApi()
        val cache = File(System.getProperty("java.io.tmpdir"), "mototrack-test-${System.nanoTime()}/cities.json")

        return PlaceServices(
            cities = CityCatalog(ibge, cache, ioDispatcher = dispatcher),
            routes = RouteDistanceService(geocoding, ibge, osrm)
        )
    }

    @Test
    fun suggestsCitiesWhileTheRiderTypes() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)), places = places())

        viewModel.onOriginChange("ara")

        assertEquals(
            listOf("Araras", "Araras", "Arapongas"),
            viewModel.uiState.value.originSuggestions.map { it.name }
        )
        assertTrue(viewModel.uiState.value.destinationSuggestions.isEmpty())
    }

    @Test
    fun pickingBothCitiesFillsTheDistanceByRoute() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)), places = places())

        viewModel.onOriginCitySelected(ARARAS)
        viewModel.onDestinationCitySelected(LEME)

        val state = viewModel.uiState.value
        assertEquals("Araras, SP", state.origin)
        assertEquals("Leme, SP", state.destination)
        assertEquals("21,9", state.distance)
        assertEquals(DistanceSource.ROUTE, state.distanceSource)
        assertFalse(state.isCalculatingDistance)
        assertTrue(state.originSuggestions.isEmpty())
    }

    @Test
    fun theRidersOwnDistanceIsNeverOverwritten() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)), places = places())

        viewModel.onDistanceChange("30")
        viewModel.onOriginCitySelected(ARARAS)
        viewModel.onDestinationCitySelected(LEME)

        assertEquals("30", viewModel.uiState.value.distance)
        assertNull(viewModel.uiState.value.distanceSource)
    }

    @Test
    fun typingTheDistanceAfterwardsMakesItTheRiders() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)), places = places())

        viewModel.onOriginCitySelected(ARARAS)
        viewModel.onDestinationCitySelected(LEME)
        viewModel.onDistanceChange("25")
        viewModel.onDestinationCitySelected(City(5, "Rio Claro", "SP"))

        assertEquals("25", viewModel.uiState.value.distance)
    }

    @Test
    fun changingACityRecalculatesTheDistanceTheAppFilledIn() = runTest(dispatcher) {
        val osrm = FakeOsrmApi(kilometres = 21.9)
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)), places = places(osrm = osrm))

        viewModel.onOriginCitySelected(ARARAS)
        viewModel.onDestinationCitySelected(LEME)
        assertEquals("21,9", viewModel.uiState.value.distance)

        // Swapping the cities back and forth recalculates nothing the rider typed, but a
        // new destination is a new route.
        viewModel.onDestinationCitySelected(ARARAS.copy(id = 99, name = "Leme", uf = "SP"))

        assertEquals(2, osrm.requests.size)
        assertEquals("21,9", viewModel.uiState.value.distance)
    }

    @Test
    fun aRouteThatCannotBeCalculatedDoesNotKeepTheOldDistance() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)), places = places())

        viewModel.onOriginCitySelected(ARARAS)
        viewModel.onDestinationCitySelected(LEME)
        assertEquals("21,9", viewModel.uiState.value.distance)

        // The fake geocoder cannot place Rio Claro, so there is no route for it.
        viewModel.onDestinationCitySelected(City(5, "Rio Claro", "SP"))

        assertEquals("", viewModel.uiState.value.distance)
        assertNull(viewModel.uiState.value.distanceSource)
    }

    @Test
    fun editingACityOutOfTheListTakesTheAutomaticDistanceAway() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)), places = places())

        viewModel.onOriginCitySelected(ARARAS)
        viewModel.onDestinationCitySelected(LEME)
        viewModel.onDestinationChange("Leme, SP e mais um pouco")

        val state = viewModel.uiState.value
        assertEquals("", state.distance)
        assertNull(state.distanceSource)
        assertNull(state.destinationCity)
    }

    @Test
    fun withoutARouteTheDistanceIsAnEstimate() = runTest(dispatcher) {
        val osrm = FakeOsrmApi().apply { failure = IOException("busy") }
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)), places = places(osrm = osrm))

        viewModel.onOriginCitySelected(ARARAS)
        viewModel.onDestinationCitySelected(LEME)

        assertEquals(DistanceSource.ESTIMATE, viewModel.uiState.value.distanceSource)
        assertTrue(viewModel.uiState.value.distance.isNotBlank())
    }

    @Test
    fun offlineLeavesTheDistanceToTheRider() = runTest(dispatcher) {
        val geocoding = FakeGeocodingApi().apply { failure = IOException("offline") }
        val viewModel = viewModel(
            motorcycles = listOf(motorcycle(1)),
            places = places(geocoding = geocoding)
        )

        viewModel.onOriginCitySelected(ARARAS)
        viewModel.onDestinationCitySelected(LEME)

        assertEquals("", viewModel.uiState.value.distance)
        assertFalse(viewModel.uiState.value.isCalculatingDistance)
    }

    @Test
    fun aPlaceTypedInFullIsRecognisedAsACity() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)), places = places())

        // As when a recent place is tapped.
        viewModel.onOriginChange("Araras, SP")

        assertEquals(ARARAS, viewModel.uiState.value.originCity)
        assertTrue(viewModel.uiState.value.originSuggestions.isEmpty())
    }

    @Test
    fun swappingTheRouteSwapsTheCitiesAndKeepsTheDistance() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)), places = places())

        viewModel.onOriginCitySelected(ARARAS)
        viewModel.onDestinationCitySelected(LEME)
        viewModel.swapRoute()

        val state = viewModel.uiState.value
        assertEquals(LEME, state.originCity)
        assertEquals(ARARAS, state.destinationCity)
        assertEquals("21,9", state.distance)
    }

    @Test
    fun withoutPlaceServicesThereAreNoSuggestions() = runTest(dispatcher) {
        val viewModel = viewModel(motorcycles = listOf(motorcycle(1)))

        viewModel.onOriginChange("ara")

        assertTrue(viewModel.uiState.value.originSuggestions.isEmpty())
    }

    @Test
    fun editingFillsTheFormWithTheTripAsTheServerHasIt() = runTest(dispatcher) {
        val tripApi = FakeTripApi().apply {
            stored = historyTrip(9, "Curitiba", "Floripa", 300.0, "2026-10-20", 2, "IN_PROGRESS")
        }

        val viewModel = viewModel(
            tripApi = tripApi,
            motorcycles = listOf(motorcycle(1), motorcycle(2)),
            editingTripId = 9
        )

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertFalse(state.isLoadingTrip)
        assertEquals("IN_PROGRESS", state.editingStatus)
        assertEquals("Curitiba", state.origin)
        assertEquals("Floripa", state.destination)
        assertEquals("300", state.distance)
        assertEquals(LocalDate.parse("2026-10-20"), state.tripDate)
        assertEquals(2L, state.selectedMotorcycleId)
        // A trip that already left takes past dates, like a registered one.
        assertEquals(TripCreateMode.COMPLETED, state.mode)
    }

    @Test
    fun editingSubmitsAnUpdateInsteadOfCreatingATrip() = runTest(dispatcher) {
        val tripApi = FakeTripApi().apply {
            stored = historyTrip(9, "Curitiba", "Floripa", 300.0, "2026-10-20", 1, "COMPLETED")
        }
        val viewModel = viewModel(
            tripApi = tripApi,
            motorcycles = listOf(motorcycle(1)),
            editingTripId = 9
        )

        viewModel.onDestinationChange("Joinville")
        viewModel.onDistanceChange("180,5")
        viewModel.submit()

        val state = viewModel.uiState.value
        assertTrue(state.isUpdated)
        assertFalse(state.isCreated)
        assertEquals(
            listOf(
                9L to UpdateTripRequestDTO(
                    origin = "Curitiba",
                    destination = "Joinville",
                    distanceKm = 180.5,
                    terrain = "ASPHALT",
                    tripDate = "2026-10-20",
                    motorcycleId = 1
                )
            ),
            tripApi.updated
        )
        assertTrue(tripApi.scheduled.isEmpty())
        assertTrue(tripApi.completed.isEmpty())
    }

    @Test
    fun editingCannotChangeTheMode() = runTest(dispatcher) {
        val tripApi = FakeTripApi().apply {
            stored = historyTrip(9, "A", "B", 10.0, "2026-10-20", 1, "COMPLETED")
        }
        val viewModel = viewModel(
            tripApi = tripApi,
            motorcycles = listOf(motorcycle(1)),
            editingTripId = 9
        )

        viewModel.onModeSelected(TripCreateMode.PLANNED)

        assertEquals(TripCreateMode.COMPLETED, viewModel.uiState.value.mode)
    }

    @Test
    fun failedTripLoadBlocksSavingUntilItIsRetried() = runTest(dispatcher) {
        val tripApi = FakeTripApi().apply { loadFailure = IOException("offline") }
        val viewModel = viewModel(
            tripApi = tripApi,
            motorcycles = listOf(motorcycle(1)),
            editingTripId = 9
        )

        assertNotNull(viewModel.uiState.value.loadTripErrorMessage)

        viewModel.submit()
        assertTrue(tripApi.updated.isEmpty())

        tripApi.loadFailure = null
        tripApi.stored = historyTrip(9, "A", "B", 10.0, "2026-10-20", 1, "COMPLETED")
        viewModel.loadTrip()

        assertNull(viewModel.uiState.value.loadTripErrorMessage)
        assertEquals("A", viewModel.uiState.value.origin)
    }

    @Test
    fun defaultMotorcycleBeatsTheLatestTrip() = runTest(dispatcher) {
        val tripApi = FakeTripApi().apply {
            history = listOf(historyTrip(1, "A", "B", 10.0, "2026-09-01", 1))
        }
        val viewModel = viewModel(
            tripApi = tripApi,
            motorcycles = listOf(motorcycle(1), motorcycle(2)),
            preferences = FakeMotorcyclePreferences(defaultId = 2)
        )

        assertEquals(2L, viewModel.uiState.value.selectedMotorcycleId)
    }

    @Test
    fun requestedMotorcycleBeatsTheDefault() = runTest(dispatcher) {
        val viewModel = viewModel(
            motorcycles = listOf(motorcycle(1), motorcycle(2)),
            preferences = FakeMotorcyclePreferences(defaultId = 2),
            requestedMotorcycleId = 1
        )

        assertEquals(1L, viewModel.uiState.value.selectedMotorcycleId)
    }

    @Test
    fun staleDefaultIsIgnored() = runTest(dispatcher) {
        val viewModel = viewModel(
            motorcycles = listOf(motorcycle(1), motorcycle(2)),
            preferences = FakeMotorcyclePreferences(defaultId = 99)
        )

        assertNull(viewModel.uiState.value.selectedMotorcycleId)
    }

    @Test
    fun userChoiceIsNotOverriddenByLateDefault() = runTest(dispatcher) {
        val viewModel = viewModel(
            motorcycles = listOf(motorcycle(1), motorcycle(2)),
            preferences = FakeMotorcyclePreferences()
        )

        viewModel.onMotorcycleSelected(1)

        assertEquals(1L, viewModel.uiState.value.selectedMotorcycleId)
    }

    class FakeMotorcyclePreferences(
        defaultId: Long? = null
    ) : MotorcyclePreferences {
        private val state = MutableStateFlow(defaultId)

        override val defaultMotorcycleId: Flow<Long?> = state

        override suspend fun setDefaultMotorcycleId(motorcycleId: Long) {
            state.value = motorcycleId
        }
    }

    private class FakeTripApi : TripApiService {
        val scheduled = mutableListOf<CreateTripRequestDTO>()
        val completed = mutableListOf<CreateTripRequestDTO>()
        var onSchedule: suspend () -> Unit = {}
        var history: List<TripResponseDTO> = emptyList()
        var stored: TripResponseDTO? = null
        var loadFailure: Exception? = null
        val updated = mutableListOf<Pair<Long, UpdateTripRequestDTO>>()

        override suspend fun updateTrip(
            tripId: Long,
            request: UpdateTripRequestDTO
        ): TripResponseDTO {
            updated += tripId to request
            return stored ?: throw AssertionError("No stored trip")
        }

        override suspend fun correctTripTimestamps(
            tripId: Long,
            request: br.dev.guisleri.mototrack.data.model.trip.UpdateTripTimestampsRequestDTO
        ): TripResponseDTO =
            throw AssertionError("Unexpected timestamps correction")

        override suspend fun getTrips(): List<TripResponseDTO> = history

        override suspend fun getTripById(tripId: Long): TripResponseDTO {
            loadFailure?.let { throw it }
            return stored ?: throw AssertionError("Unexpected detail request")
        }

        override suspend fun scheduleTrip(
            request: CreateTripRequestDTO
        ): TripResponseDTO {
            onSchedule()
            scheduled += request
            return trip(request, "PLANNED")
        }

        override suspend fun registerCompletedTrip(
            request: CreateTripRequestDTO
        ): TripResponseDTO {
            completed += request
            return trip(request, "COMPLETED")
        }

        override suspend fun deleteTrip(tripId: Long) {
            throw AssertionError("Unexpected trip deletion")
        }

        override suspend fun updateTripStatus(
            tripId: Long,
            request: UpdateTripStatusRequestDTO
        ) {
            throw AssertionError("Unexpected status update")
        }

        private fun trip(request: CreateTripRequestDTO, status: String) =
            TripResponseDTO(
                id = 1,
                origin = request.origin,
                destination = request.destination,
                distanceKm = request.distanceKm,
                status = status,
                terrain = request.terrain,
                tripDate = request.tripDate,
                motorcycle = motorcycle(request.motorcycleId)
            )
    }

    private class FakeMotorcycleApi(
        private val motorcycles: List<MotorcycleResponseDTO>
    ) : MotorcycleApiService {

        override suspend fun updateMotorcycle(
            motorcycleId: Long,
            request: br.dev.guisleri.mototrack.data.model.motorcycle.UpdateMotorcycleRequestDTO
        ): br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO =
            throw AssertionError("Unexpected motorcycle update")

        override suspend fun getMotorcycles(): List<MotorcycleResponseDTO> =
            motorcycles

        override suspend fun createMotorcycle(
            request: br.dev.guisleri.mototrack.data.model.motorcycle.CreateMotorcycleRequestDTO
        ): MotorcycleResponseDTO =
            throw AssertionError("Unexpected motorcycle creation")

        override suspend fun deleteMotorcycle(motorcycleId: Long) {
            throw AssertionError("Unexpected motorcycle deletion")
        }

        override suspend fun getMotorcycleStatistics(
            motorcycleId: Long
        ): MotorcycleStatisticsResponseDTO =
            throw AssertionError("Unexpected statistics request")
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
