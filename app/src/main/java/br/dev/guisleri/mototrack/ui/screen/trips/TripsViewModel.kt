package br.dev.guisleri.mototrack.ui.screen.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.data.repository.TripRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class TripsViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<TripsUiState>(
            TripsUiState.Loading
        )

    val uiState: StateFlow<TripsUiState> =
        _uiState.asStateFlow()

    private val _isRefreshing =
        MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean> =
        _isRefreshing.asStateFlow()

    private var tripsJob: Job? = null

    init {
        loadTrips()
    }

    fun loadTrips() {
        tripsJob?.cancel()
        _isRefreshing.value = false
        _uiState.value = TripsUiState.Loading

        tripsJob = viewModelScope.launch {
            try {
                publishTrips(tripRepository.getTrips())
            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                _uiState.value = TripsUiState.Error(
                    message = "Não foi possível comunicar com o servidor."
                )

            } catch (exception: Exception) {
                _uiState.value = TripsUiState.Error(
                    message = "Não foi possível carregar as viagens."
                )
            }
        }
    }

    fun refreshTrips() {
        if (tripsJob?.isActive == true) return

        // Nothing to keep on screen yet (e.g. empty list after creating the
        // first trip), so reload from scratch instead of refreshing in place.
        val previousSuccess = _uiState.value as? TripsUiState.Success
            ?: run {
                loadTrips()
                return
            }

        _isRefreshing.value = true
        tripsJob = viewModelScope.launch {
            try {
                publishTrips(tripRepository.getTrips())
            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                _uiState.value = previousSuccess.copy(
                    refreshErrorMessage =
                        "Não foi possível comunicar com o servidor."
                )

            } catch (exception: Exception) {
                _uiState.value = previousSuccess.copy(
                    refreshErrorMessage = "Não foi possível atualizar as viagens."
                )

            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun publishTrips(trips: List<TripResponseDTO>) {
        _uiState.value = if (trips.isEmpty()) {
            TripsUiState.Empty
        } else {
            TripsUiState.Success(trips = trips)
        }
    }
}