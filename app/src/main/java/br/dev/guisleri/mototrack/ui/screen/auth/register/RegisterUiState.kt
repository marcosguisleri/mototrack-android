package br.dev.guisleri.mototrack.ui.screen.auth.register

sealed interface RegisterUiState {

    data object Idle : RegisterUiState

    data object Loading : RegisterUiState

    data object Success : RegisterUiState

    data class Error(
        val message: String
    ) : RegisterUiState
}
