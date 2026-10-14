package br.dev.guisleri.mototrack.ui.screen.motorcycles

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO

sealed interface MotorcyclesUiState {

    data object Loading : MotorcyclesUiState

    data object Empty : MotorcyclesUiState

    data class Success(
        val motorcycles: List<MotorcycleResponseDTO>,
        val selectedMotorcycleId: Long,
        val statisticsUiState: MotorcycleStatisticsUiState
    ) : MotorcyclesUiState

    data class Error(
        val message: String
    ) : MotorcyclesUiState
}