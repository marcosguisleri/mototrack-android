package br.dev.guisleri.mototrack.ui.screen.home

import br.dev.guisleri.mototrack.data.model.home.NextTripDTO
import br.dev.guisleri.mototrack.data.model.home.TripSummaryDTO

sealed interface HomeHighlightState {

    data class InProgress(
        val trip: TripSummaryDTO
    ) : HomeHighlightState

    data object NoMotorcycle : HomeHighlightState

    data class UpcomingTrip(
        val trip: NextTripDTO
    ) : HomeHighlightState

    data object NoUpcomingTrip : HomeHighlightState
}