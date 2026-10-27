package br.dev.guisleri.mototrack.ui.screen.profile

import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO

data class ProfileUiState(
    val user: UserResponseDTO? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // The dialog that edits the user's name.
    val isEditingName: Boolean = false,
    val isSavingName: Boolean = false,
    val nameErrorMessage: String? = null
)
