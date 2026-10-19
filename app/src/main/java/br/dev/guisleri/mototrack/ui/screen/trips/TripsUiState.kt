package br.dev.guisleri.mototrack.ui.screen.trips

import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO

sealed interface TripsUiState {

    data object Loading : TripsUiState

    data object Empty : TripsUiState

    data class Success(
        val trips: List<TripResponseDTO>
    ) : TripsUiState

    data class Error(
        val message: String
    ) : TripsUiState

}
