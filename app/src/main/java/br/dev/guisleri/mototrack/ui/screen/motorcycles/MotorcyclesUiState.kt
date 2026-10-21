package br.dev.guisleri.mototrack.ui.screen.motorcycles

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO

sealed interface MotorcyclesUiState {

    data object Loading : MotorcyclesUiState

    data object Empty : MotorcyclesUiState

    data class Success(
        val motorcycles: List<MotorcycleResponseDTO>,
        val selectedMotorcycleId: Long,
        val defaultMotorcycleId: Long? = null,
        val statisticsUiState: MotorcycleStatisticsUiState,
        val isDeleting: Boolean = false,
        val deleteIssue: MotorcycleDeleteIssue? = null
    ) : MotorcyclesUiState

    data class Error(
        val message: String
    ) : MotorcyclesUiState
}

/** Why a motorcycle could not be deleted. */
sealed interface MotorcycleDeleteIssue {

    /** The server refuses to delete a motorcycle that still has trips. */
    data object HasTrips : MotorcycleDeleteIssue

    data class Failed(
        val message: String
    ) : MotorcycleDeleteIssue
}
