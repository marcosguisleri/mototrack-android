package br.dev.guisleri.mototrack.ui.screen.profile

import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO

data class ProfileUiState(
    val user: UserResponseDTO? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
