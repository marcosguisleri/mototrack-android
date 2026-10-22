package br.dev.guisleri.mototrack.ui.screen.motorcycles

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO

sealed interface MotorcycleStatisticsUiState {

    data object Loading : MotorcycleStatisticsUiState

    data class Success(
        val statistics: MotorcycleStatisticsResponseDTO
    ) : MotorcycleStatisticsUiState

    data class Error(
        val message: String
    ) : MotorcycleStatisticsUiState
}