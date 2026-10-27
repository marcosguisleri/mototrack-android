package br.dev.guisleri.mototrack.ui.screen.auth.forgot

enum class ForgotPasswordStep {
    /** Asking for the e-mail the code goes to. */
    EMAIL,

    /** The code was sent: asking for it and for the new password. */
    CODE
}

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.EMAIL,
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Seconds until another code can be asked for.
    val resendSecondsLeft: Int = 0,
    val isReset: Boolean = false
)
