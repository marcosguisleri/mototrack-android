package br.dev.guisleri.mototrack.ui.screen.trips.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.model.common.ApiErrorResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.CreateTripRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.UpdateTripRequestDTO
import br.dev.guisleri.mototrack.data.local.MotorcyclePreferences
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository
import kotlinx.coroutines.flow.first
import br.dev.guisleri.mototrack.data.repository.TripRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

private const val MAX_TEXT_LENGTH = 255
private const val MAX_DISTANCE_INTEGER_DIGITS = 5
private const val MAX_RECENT_PLACES = 6

private val apiErrorJson = Json {
    ignoreUnknownKeys = true
}

class TripCreateViewModel(
    private val tripRepository: TripRepository,
    private val motorcycleRepository: MotorcycleRepository,
    private val motorcyclePreferences: MotorcyclePreferences,
    initialMode: TripCreateMode = TripCreateMode.PLANNED,
    private val requestedMotorcycleId: Long? = null,
    private val editingTripId: Long? = null
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            TripCreateUiState(
                mode = initialMode,
                editingTripId = editingTripId,
                isLoadingTrip = editingTripId != null
            )
        )

    val uiState: StateFlow<TripCreateUiState> =
        _uiState.asStateFlow()

    private var motorcyclesJob: Job? = null

    // Learned from the user's previous trips; they only make suggestions.
    private var knownRoutes: Map<String, Double> = emptyMap()
    private var lastUsedMotorcycleId: Long? = null
    private var defaultMotorcycleId: Long? = null
    private var userPickedMotorcycle = false

    private var tripJob: Job? = null

    init {
        loadMotorcycles()
        loadHistory()
        loadDefaultMotorcycle()
        loadTrip()
    }

    /** When editing, fills the form with the trip as the server has it. */
    fun loadTrip() {
        val tripId = editingTripId ?: return

        tripJob?.cancel()

        _uiState.value = _uiState.value.copy(
            isLoadingTrip = true,
            loadTripErrorMessage = null
        )

        tripJob = viewModelScope.launch {
            try {
                val trip = tripRepository.getTripById(tripId)

                // The trip's own motorcycle wins over the defaults.
                userPickedMotorcycle = true

                _uiState.value = _uiState.value.copy(
                    isLoadingTrip = false,
                    mode = if (trip.status == "PLANNED") {
                        TripCreateMode.PLANNED
                    } else {
                        TripCreateMode.COMPLETED
                    },
                    editingStatus = trip.status,
                    origin = trip.origin,
                    destination = trip.destination,
                    distance = formatDistanceInput(trip.distanceKm),
                    tripDate = runCatching { LocalDate.parse(trip.tripDate) }
                        .getOrDefault(LocalDate.now()),
                    terrain = TripCreateTerrain.entries
                        .firstOrNull { it.apiValue == trip.terrain }
                        ?: TripCreateTerrain.ASPHALT,
                    selectedMotorcycleId = trip.motorcycle.id
                ).withDistanceSuggestion()

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                _uiState.value = _uiState.value.copy(
                    isLoadingTrip = false,
                    loadTripErrorMessage = "Não foi possível comunicar com o servidor."
                )

            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingTrip = false,
                    loadTripErrorMessage = "Não foi possível carregar a viagem."
                )
            }
        }
    }

    private fun loadDefaultMotorcycle() {
        viewModelScope.launch {
            try {
                defaultMotorcycleId = motorcyclePreferences.defaultMotorcycleId.first()

                _uiState.value = withDefaultMotorcycle(_uiState.value)

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                // The preference is optional: fall back to the other rules.
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            try {
                val trips = tripRepository.getTrips()

                val places = (trips.map { it.origin } + trips.map { it.destination })
                    .groupingBy { it.trim() }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .map { it.key }
                    .take(MAX_RECENT_PLACES)

                knownRoutes = trips.associate { trip ->
                    routeKey(trip.origin, trip.destination) to trip.distanceKm
                }

                lastUsedMotorcycleId = trips
                    .maxByOrNull { it.tripDate }
                    ?.motorcycle
                    ?.id

                _uiState.value = _uiState.value.let { state ->
                    withDefaultMotorcycle(state).copy(
                        recentPlaces = places
                    ).withDistanceSuggestion()
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                // Suggestions are optional: the form works without them.
            }
        }
    }

    /**
     * Picks the motorcycle until the user chooses one: the one asked for by the
     * caller, then the default, then the one used in the latest trip, then the
     * only one. The loads finish in any order, so this runs after each of them.
     */
    private fun withDefaultMotorcycle(state: TripCreateUiState): TripCreateUiState {
        if (userPickedMotorcycle) return state

        val loaded = state.motorcycles as? TripCreateMotorcyclesState.Loaded
            ?: return state

        fun Long?.ifOwned(): Long? =
            this?.takeIf { id -> loaded.motorcycles.any { it.id == id } }

        val preferred = requestedMotorcycleId.ifOwned()
            ?: defaultMotorcycleId.ifOwned()
            ?: lastUsedMotorcycleId.ifOwned()
            ?: loaded.motorcycles.singleOrNull()?.id

        return state.copy(selectedMotorcycleId = preferred)
    }

    private fun TripCreateUiState.withDistanceSuggestion(): TripCreateUiState {
        val suggestion = if (distance.isBlank()) {
            knownRoutes[routeKey(origin, destination)]
                ?: knownRoutes[routeKey(destination, origin)]
        } else {
            null
        }

        return copy(distanceSuggestion = suggestion)
    }

    private fun routeKey(origin: String, destination: String): String =
        "${origin.trim().lowercase()}|${destination.trim().lowercase()}"

    fun swapRoute() {
        val state = _uiState.value
        if (state.isSubmitting) return

        _uiState.value = state.copy(
            origin = state.destination,
            destination = state.origin,
            originError = null,
            destinationError = null,
            submitErrorMessage = null
        ).withDistanceSuggestion()
    }

    fun applyDistanceSuggestion() {
        val suggestion = _uiState.value.distanceSuggestion ?: return

        onDistanceChange(formatDistanceInput(suggestion))
    }

    private fun formatDistanceInput(distanceKm: Double): String =
        if (distanceKm % 1.0 == 0.0) {
            distanceKm.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.1f", distanceKm).replace('.', ',')
        }

    fun loadMotorcycles() {
        motorcyclesJob?.cancel()

        _uiState.value = _uiState.value.copy(
            motorcycles = TripCreateMotorcyclesState.Loading
        )

        motorcyclesJob = viewModelScope.launch {
            try {
                val motorcycles =
                    motorcycleRepository.getMotorcycles()

                _uiState.value = withDefaultMotorcycle(
                    _uiState.value.copy(
                        motorcycles =
                            TripCreateMotorcyclesState.Loaded(motorcycles)
                    )
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                _uiState.value = _uiState.value.copy(
                    motorcycles = TripCreateMotorcyclesState.Error(
                        message = "Não foi possível comunicar com o servidor."
                    )
                )

            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    motorcycles = TripCreateMotorcyclesState.Error(
                        message = "Não foi possível carregar suas motos."
                    )
                )
            }
        }
    }

    fun onModeSelected(mode: TripCreateMode) {
        val state = _uiState.value
        if (state.mode == mode || state.isSubmitting || state.isEditing) return

        val today = LocalDate.now()

        // The backend only accepts future dates when planning and past dates when
        // registering, so a date that is invalid for the new mode falls back to today.
        val tripDate = when {
            mode == TripCreateMode.PLANNED && state.tripDate.isBefore(today) -> today
            mode == TripCreateMode.COMPLETED && state.tripDate.isAfter(today) -> today
            else -> state.tripDate
        }

        _uiState.value = state.copy(
            mode = mode,
            tripDate = tripDate,
            submitErrorMessage = null
        )
    }

    fun onOriginChange(origin: String) {
        _uiState.value = _uiState.value.copy(
            origin = origin.take(MAX_TEXT_LENGTH),
            originError = null,
            submitErrorMessage = null
        ).withDistanceSuggestion()
    }

    fun onDestinationChange(destination: String) {
        _uiState.value = _uiState.value.copy(
            destination = destination.take(MAX_TEXT_LENGTH),
            destinationError = null,
            submitErrorMessage = null
        ).withDistanceSuggestion()
    }

    fun onDistanceChange(distance: String) {
        _uiState.value = _uiState.value.copy(
            distance = sanitizeDistance(distance),
            distanceError = null,
            submitErrorMessage = null
        ).withDistanceSuggestion()
    }

    fun onTripDateSelected(tripDate: LocalDate) {
        _uiState.value = _uiState.value.copy(
            tripDate = tripDate,
            submitErrorMessage = null
        )
    }

    fun onTerrainSelected(terrain: TripCreateTerrain) {
        _uiState.value = _uiState.value.copy(
            terrain = terrain,
            submitErrorMessage = null
        )
    }

    fun onMotorcycleSelected(motorcycleId: Long) {
        userPickedMotorcycle = true

        _uiState.value = _uiState.value.copy(
            selectedMotorcycleId = motorcycleId,
            motorcycleError = null,
            submitErrorMessage = null
        )
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting || state.isLoadingTrip || state.loadTripErrorMessage != null) {
            return
        }

        val origin = state.origin.trim()
        val destination = state.destination.trim()
        val distanceKm = parseDistance(state.distance)
        val motorcycleId = state.selectedMotorcycleId

        val originError =
            "Informe a origem.".takeIf { origin.isEmpty() }
        val destinationError =
            "Informe o destino.".takeIf { destination.isEmpty() }
        val distanceError =
            "Informe uma distância maior que zero."
                .takeIf { distanceKm == null || distanceKm <= 0.0 }
        val motorcycleError =
            "Selecione a moto da viagem.".takeIf { motorcycleId == null }

        if (
            originError != null ||
            destinationError != null ||
            distanceError != null ||
            motorcycleError != null ||
            distanceKm == null ||
            motorcycleId == null
        ) {
            _uiState.value = state.copy(
                originError = originError,
                destinationError = destinationError,
                distanceError = distanceError,
                motorcycleError = motorcycleError
            )
            return
        }

        val request = CreateTripRequestDTO(
            origin = origin,
            destination = destination,
            distanceKm = distanceKm,
            terrain = state.terrain.apiValue,
            tripDate = state.tripDate.toString(),
            motorcycleId = motorcycleId
        )

        val editingId = state.editingTripId

        _uiState.value = state.copy(
            isSubmitting = true,
            submitErrorMessage = null
        )

        viewModelScope.launch {
            try {
                if (editingId != null) {
                    tripRepository.updateTrip(
                        tripId = editingId,
                        request = UpdateTripRequestDTO(
                            origin = request.origin,
                            destination = request.destination,
                            distanceKm = request.distanceKm,
                            terrain = request.terrain,
                            tripDate = request.tripDate,
                            motorcycleId = request.motorcycleId
                        )
                    )
                } else {
                    when (state.mode) {
                        TripCreateMode.PLANNED ->
                            tripRepository.scheduleTrip(request)

                        TripCreateMode.COMPLETED ->
                            tripRepository.registerCompletedTrip(request)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    isCreated = editingId == null,
                    isUpdated = editingId != null
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitErrorMessage =
                        getApiErrorMessage(exception)
                            ?: defaultSubmitError(state)
                )

            } catch (exception: IOException) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitErrorMessage =
                        "Não foi possível comunicar com o servidor."
                )

            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitErrorMessage = defaultSubmitError(state)
                )
            }
        }
    }

    private fun defaultSubmitError(state: TripCreateUiState): String =
        if (state.isEditing) {
            "Não foi possível salvar as alterações."
        } else when (state.mode) {
            TripCreateMode.PLANNED ->
                "Não foi possível criar a viagem."

            TripCreateMode.COMPLETED ->
                "Não foi possível registrar a viagem."
        }

    // Keeps only digits and a single decimal separator, shown as a comma, with at
    // most one decimal place (e.g. "412,5").
    private fun sanitizeDistance(raw: String): String {
        val filtered = raw.filter { char ->
            char.isDigit() || char == ',' || char == '.'
        }

        val separatorIndex = filtered.indexOfFirst { char ->
            char == ',' || char == '.'
        }

        if (separatorIndex == -1) {
            return filtered.take(MAX_DISTANCE_INTEGER_DIGITS)
        }

        val integerPart = filtered
            .substring(0, separatorIndex)
            .take(MAX_DISTANCE_INTEGER_DIGITS)

        val decimalPart = filtered
            .substring(separatorIndex + 1)
            .filter { char -> char.isDigit() }
            .take(1)

        return "$integerPart,$decimalPart"
    }

    private fun parseDistance(distance: String): Double? =
        distance
            .replace(',', '.')
            .trimEnd('.')
            .toDoubleOrNull()

    private fun getApiErrorMessage(
        exception: HttpException
    ): String? {
        val body =
            exception.response()
                ?.errorBody()
                ?.string()
                ?: return null

        return runCatching {
            apiErrorJson.decodeFromString<ApiErrorResponseDTO>(
                body
            ).message
        }.getOrNull()
    }
}
