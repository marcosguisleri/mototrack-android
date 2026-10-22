package br.dev.guisleri.mototrack.ui.screen.trips.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.model.common.ApiErrorResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.UpdateTripRequestDTO
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
import java.time.Instant
import kotlin.math.abs

private val apiErrorJson = Json {
    ignoreUnknownKeys = true
}

class TripDetailsViewModel(
    private val tripId: Long,
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<TripDetailsUiState>(
            TripDetailsUiState.Loading
        )

    val uiState: StateFlow<TripDetailsUiState> =
        _uiState.asStateFlow()

    private var loadJob: Job? = null

    // The status the server asked a date update for: starting a trip on another
    // day, or completing one whose date is still in the future.
    private var statusAwaitingDateUpdate: String? = null

    // Kept across the date confirmation, which repeats the completion.
    private var pendingActualDistanceKm: Double? = null

    init {
        loadTrip()
    }

    fun loadTrip() {
        if ((_uiState.value as? TripDetailsUiState.Success)?.isUpdatingStatus == true) {
            return
        }
        loadJob?.cancel()
        _uiState.value =
            TripDetailsUiState.Loading

        loadJob = viewModelScope.launch {
            try {
                val trip =
                    tripRepository.getTripById(
                        tripId = tripId
                    )

                _uiState.value =
                    TripDetailsUiState.Success(
                        trip = trip
                    )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                _uiState.value =
                    TripDetailsUiState.Error(
                        message =
                            "Não foi possível comunicar com o servidor."
                    )

            } catch (exception: Exception) {
                _uiState.value =
                    TripDetailsUiState.Error(
                        message =
                            "Não foi possível carregar a viagem."
                    )
            }
        }
    }

    /**
     * Reloads the trip without the loading state, for when the screen comes back
     * (from editing the trip, or from the background) and what it shows may be old.
     */
    fun refresh() {
        val state = _uiState.value as? TripDetailsUiState.Success ?: return
        if (state.isUpdatingStatus || state.isDeleting || state.isSavingTimes) return

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val trip = tripRepository.getTripById(tripId = tripId)

                (_uiState.value as? TripDetailsUiState.Success)?.let { current ->
                    _uiState.value = current.copy(trip = trip)
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                // What is on screen is still the last thing the server said.
            }
        }
    }

    fun openTimesEditor() {
        val state = _uiState.value as? TripDetailsUiState.Success ?: return
        if (state.trip.status == "PLANNED") return

        _uiState.value = state.copy(
            isEditingTimes = true,
            timesErrorMessage = null
        )
    }

    fun dismissTimesEditor() {
        val state = _uiState.value as? TripDetailsUiState.Success ?: return
        if (state.isSavingTimes) return

        _uiState.value = state.copy(
            isEditingTimes = false,
            timesErrorMessage = null
        )
    }

    /** A null clears that time; a trip in progress has no completion time. */
    fun correctTimes(startedAt: Instant?, completedAt: Instant?) {
        val state = _uiState.value as? TripDetailsUiState.Success ?: return
        if (state.isSavingTimes) return

        _uiState.value = state.copy(
            isSavingTimes = true,
            timesErrorMessage = null
        )

        viewModelScope.launch {
            try {
                val trip = tripRepository.correctTripTimestamps(
                    tripId = tripId,
                    startedAt = startedAt?.toString(),
                    completedAt = completedAt?.toString()
                )

                _uiState.value = currentSuccessState(state).copy(
                    trip = trip,
                    isEditingTimes = false,
                    isSavingTimes = false,
                    timesErrorMessage = null
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                showTimesError(
                    state,
                    getApiErrorMessage(exception)
                        ?: "Não foi possível ajustar os horários."
                )

            } catch (exception: IOException) {
                showTimesError(
                    state,
                    "Não foi possível comunicar com o servidor."
                )

            } catch (exception: Exception) {
                showTimesError(
                    state,
                    "Não foi possível ajustar os horários."
                )
            }
        }
    }

    private fun showTimesError(
        fallback: TripDetailsUiState.Success,
        message: String
    ) {
        _uiState.value = currentSuccessState(fallback).copy(
            isSavingTimes = false,
            timesErrorMessage = message
        )
    }

    fun startTrip() {
        updateStatus(
            status = "IN_PROGRESS",
            updateTripDateToToday = false
        )
    }

    /**
     * Completes the trip. When [actualDistanceKm] is given the trip's distance is
     * corrected once it is completed (a completed trip can no longer have a future
     * date, so the status goes first).
     */
    fun completeTrip(actualDistanceKm: Double? = null) {
        pendingActualDistanceKm = actualDistanceKm

        updateStatus(
            status = "COMPLETED"
        )
    }

    fun deleteTrip() {
        val currentState =
            _uiState.value as? TripDetailsUiState.Success
                ?: return

        if (currentState.isUpdatingStatus || currentState.isDeleting) {
            return
        }

        _uiState.value =
            currentState.copy(
                isDeleting = true,
                actionErrorMessage = null
            )

        viewModelScope.launch {
            try {
                tripRepository.deleteTrip(tripId)

                _uiState.value =
                    currentSuccessState(currentState).copy(
                        isDeleting = false,
                        isDeleted = true
                    )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                showDeleteError(
                    currentState,
                    getApiErrorMessage(exception)
                        ?: "Não foi possível excluir a viagem."
                )

            } catch (exception: IOException) {
                showDeleteError(
                    currentState,
                    "Não foi possível comunicar com o servidor."
                )

            } catch (exception: Exception) {
                showDeleteError(
                    currentState,
                    "Não foi possível excluir a viagem."
                )
            }
        }
    }

    private fun showDeleteError(
        fallback: TripDetailsUiState.Success,
        message: String
    ) {
        _uiState.value =
            currentSuccessState(fallback).copy(
                isDeleting = false,
                actionErrorMessage = message
            )
    }

    fun celebrationFinished() {
        val currentState =
            _uiState.value as? TripDetailsUiState.Success
                ?: return

        if (currentState.celebration != null) {
            _uiState.value = currentState.copy(celebration = null)
        }
    }

    fun confirmTripDateUpdate() {
        val status = statusAwaitingDateUpdate ?: return

        updateStatus(
            status = status,
            updateTripDateToToday = true
        )
    }

    fun dismissTripDateUpdateConfirmation() {
        statusAwaitingDateUpdate = null
        pendingActualDistanceKm = null

        val currentState =
            _uiState.value as? TripDetailsUiState.Success
                ?: return

        _uiState.value =
            currentState.copy(
                requiresTripDateUpdateConfirmation = false
            )
    }

    private fun updateStatus(
        status: String,
        updateTripDateToToday: Boolean = false
    ) {
        val currentState =
            _uiState.value as? TripDetailsUiState.Success
                ?: return

        if (currentState.isUpdatingStatus || currentState.isDeleting) {
            return
        }

        _uiState.value =
            currentState.copy(
                isUpdatingStatus = true,
                actionErrorMessage = null,
                requiresTripDateUpdateConfirmation = false
            )

        viewModelScope.launch {
            try {
                tripRepository.updateTripStatus(
                    tripId = tripId,
                    status = status,
                    updateTripDateToToday = updateTripDateToToday
                )

                val distanceError =
                    if (status == "COMPLETED") saveActualDistance() else null

                val updatedTrip =
                    tripRepository.getTripById(
                        tripId = tripId
                    )

                _uiState.value =
                    currentState.copy(
                        trip = updatedTrip,
                        isUpdatingStatus = false,
                        actionErrorMessage = distanceError,
                        requiresTripDateUpdateConfirmation = false,
                        celebration = if (status == "COMPLETED") {
                            TripCelebration.COMPLETED
                        } else {
                            TripCelebration.STARTED
                        }
                    )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {

                if (
                    exception.code() == 409 &&
                    (status == "IN_PROGRESS" || status == "COMPLETED") &&
                    !updateTripDateToToday
                ) {
                    statusAwaitingDateUpdate = status

                    _uiState.value =
                        currentSuccessState(currentState).copy(
                            isUpdatingStatus = false,
                            actionErrorMessage = null,
                            requiresTripDateUpdateConfirmation = true
                        )

                    return@launch
                }

                _uiState.value =
                    currentSuccessState(currentState).copy(
                        isUpdatingStatus = false,
                        actionErrorMessage =
                            getApiErrorMessage(exception)
                                ?: "Não foi possível alterar o status da viagem."
                    )

            } catch (exception: IOException) {
                _uiState.value =
                    currentSuccessState(currentState).copy(
                        isUpdatingStatus = false,
                        actionErrorMessage =
                            "Não foi possível comunicar com o servidor."
                    )

            } catch (exception: Exception) {
                _uiState.value =
                    currentSuccessState(currentState).copy(
                        isUpdatingStatus = false,
                        actionErrorMessage =
                            "Não foi possível alterar o status da viagem."
                    )
            }
        }
    }

    /** The message to show when the trip was completed but its distance could not be saved. */
    private suspend fun saveActualDistance(): String? {
        val distanceKm = pendingActualDistanceKm ?: return null
        pendingActualDistanceKm = null

        return try {
            val trip = tripRepository.getTripById(tripId = tripId)

            if (abs(trip.distanceKm - distanceKm) >= 0.05) {
                tripRepository.updateTrip(
                    tripId = tripId,
                    request = UpdateTripRequestDTO(
                        origin = trip.origin,
                        destination = trip.destination,
                        distanceKm = distanceKm,
                        terrain = trip.terrain,
                        tripDate = trip.tripDate,
                        motorcycleId = trip.motorcycle.id
                    )
                )
            }

            null

        } catch (exception: CancellationException) {
            throw exception

        } catch (exception: Exception) {
            "A viagem foi concluída, mas não foi possível salvar a distância percorrida."
        }
    }

    private fun currentSuccessState(
        fallback: TripDetailsUiState.Success
    ): TripDetailsUiState.Success =
        (_uiState.value as? TripDetailsUiState.Success) ?: fallback

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