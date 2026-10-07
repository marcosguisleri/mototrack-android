package br.dev.guisleri.mototrack.ui.screen.home

import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Success(
        val home: HomeResponseDTO
    ) : HomeUiState

    data class Error(
        val message: String
    ) : HomeUiState
}