package br.dev.guisleri.mototrack.ui.screen.trips

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.repository.TripRepository
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

    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean> =
        _isRefreshing.asStateFlow()

    init {
        loadTrips()
    }

    fun loadTrips() {
        _uiState.value = TripsUiState.Loading

        viewModelScope.launch {
            try {
                val trips = tripRepository.getTrips()

                if (trips.isEmpty()) {
                    _uiState.value = TripsUiState.Empty

                    return@launch
                }

                _uiState.value = TripsUiState.Success(
                    trips = trips
                )

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
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                val trips = tripRepository.getTrips()

                _uiState.value =
                    if (trips.isEmpty()) {
                        TripsUiState.Empty
                    } else {
                        TripsUiState.Success(
                            trips = trips
                        )
                    }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                Log.e(
                    "TripsViewModel",
                    "Failed to refresh trips due to network error.",
                    exception
                )

            } catch (exception: Exception) {
                Log.e(
                    "TripsViewModel",
                    "Failed to refresh trips.",
                    exception
                )

            } finally {
                _isRefreshing.value = false
            }
        }
    }

}