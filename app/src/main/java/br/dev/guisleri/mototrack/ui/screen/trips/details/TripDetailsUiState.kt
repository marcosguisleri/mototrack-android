package br.dev.guisleri.mototrack.ui.screen.trips.details

import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO

sealed interface TripDetailsUiState {

    data object Loading : TripDetailsUiState

    data class Success(
        val trip: TripResponseDTO,
        val isUpdatingStatus: Boolean = false,
        val isDeleting: Boolean = false,
        val isDeleted: Boolean = false,
        val actionErrorMessage: String? = null,
        val requiresTripDateUpdateConfirmation: Boolean = false
    ) : TripDetailsUiState

    data class Error(
        val message: String
    ) : TripDetailsUiState

}