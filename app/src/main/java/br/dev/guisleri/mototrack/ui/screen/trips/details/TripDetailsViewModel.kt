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

    fun confirmStartTripWithDateUpdate() {
        updateStatus(
            status = "IN_PROGRESS",
            updateTripDateToToday = true
        )
    }

    fun dismissTripDateUpdateConfirmation() {
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

        if (currentState.isUpdatingStatus) {
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
                    status == "IN_PROGRESS" &&
                    !updateTripDateToToday
                ) {
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