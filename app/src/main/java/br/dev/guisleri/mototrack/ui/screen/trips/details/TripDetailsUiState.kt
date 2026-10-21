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
        val requiresTripDateUpdateConfirmation: Boolean = false,
        // Set when the trip has just started or finished, until the animation is over.
        val celebration: TripCelebration? = null
    ) : TripDetailsUiState

    data class Error(
        val message: String
    ) : TripDetailsUiState

}
/** What the rider has just done to the trip, for the animation that marks it. */
enum class TripCelebration {
    STARTED,
    COMPLETED
}
