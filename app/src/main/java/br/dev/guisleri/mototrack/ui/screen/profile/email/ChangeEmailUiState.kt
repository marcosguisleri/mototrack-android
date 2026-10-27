package br.dev.guisleri.mototrack.ui.screen.profile.email

enum class ChangeEmailStep {
    /** The new e-mail and the current password. */
    REQUEST,

    /** The code sent to the new e-mail. */
    CONFIRM
}

data class ChangeEmailUiState(
    val step: ChangeEmailStep = ChangeEmailStep.REQUEST,
    val newEmail: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resendSecondsLeft: Int = 0,
    val isChanged: Boolean = false
)
