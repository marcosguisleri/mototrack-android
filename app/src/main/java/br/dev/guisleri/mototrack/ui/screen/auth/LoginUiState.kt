package br.dev.guisleri.mototrack.ui.screen.auth

import br.dev.guisleri.mototrack.data.model.auth.AuthTokenResponseDTO

sealed interface LoginUiState {

    data object Idle : LoginUiState

    data object Loading : LoginUiState

    data object Success : LoginUiState

    data class Error(
        val message: String
    ) : LoginUiState
}