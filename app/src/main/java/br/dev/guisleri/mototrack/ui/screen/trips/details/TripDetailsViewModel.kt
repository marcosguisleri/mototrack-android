package br.dev.guisleri.mototrack.ui.screen.trips.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.model.common.ApiErrorResponseDTO
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

    fun startTrip() {
        updateStatus(
            status = "IN_PROGRESS",
            updateTripDateToToday = false
        )
    }

    fun completeTrip() {
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

    fun confirmTripDateUpdate() {
        val status = statusAwaitingDateUpdate ?: return

        updateStatus(
            status = status,
            updateTripDateToToday = true
        )
    }

    fun dismissTripDateUpdateConfirmation() {
        statusAwaitingDateUpdate = null

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

                val updatedTrip =
                    tripRepository.getTripById(
                        tripId = tripId
                    )

                _uiState.value =
                    currentState.copy(
                        trip = updatedTrip,
                        isUpdatingStatus = false,
                        actionErrorMessage = null,
                        requiresTripDateUpdateConfirmation = false
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