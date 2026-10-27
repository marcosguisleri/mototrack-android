package br.dev.guisleri.mototrack.ui.screen.auth.forgot

import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.network.apiErrorMessage
import br.dev.guisleri.mototrack.data.repository.AccountRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

const val RESET_CODE_LENGTH = 6
const val MIN_PASSWORD_LENGTH = 8
private const val RESEND_COOLDOWN_SECONDS = 60

/** Recovering a forgotten password: the e-mail first, then the code and the new password. */
class ForgotPasswordViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())

    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    fun sendCode(emailText: String) {
        val state = _uiState.value
        if (state.isLoading) return

        val email = emailText.trim()

        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = state.copy(errorMessage = "Informe um e-mail válido.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                accountRepository.forgotPassword(email)

                _uiState.value = _uiState.value.copy(
                    step = ForgotPasswordStep.CODE,
                    email = email,
                    isLoading = false,
                    errorMessage = null
                )
                startCooldown()

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                showError(
                    if (exception.code() == 400) {
                        "Informe um e-mail válido."
                    } else {
                        "Não foi possível enviar o código."
                    }
                )

            } catch (exception: IOException) {
                showError("Não foi possível conectar ao servidor.")

            } catch (exception: Exception) {
                showError("Ocorreu um erro inesperado.")
            }
        }
    }

    /** Asks for another code to the same e-mail, once the wait is over. */
    fun resendCode() {
        val state = _uiState.value
        if (state.resendSecondsLeft > 0) return

        sendCode(state.email)
    }

    /** Back to the first step, to correct the e-mail. */
    fun changeEmail() {
        cooldownJob?.cancel()
        _uiState.value = ForgotPasswordUiState(email = _uiState.value.email)
    }

    fun resetPassword(codeText: String, newPassword: String, confirmPassword: String) {
        val state = _uiState.value
        if (state.isLoading) return

        val code = codeText.trim()

        val problem = when {
            code.length != RESET_CODE_LENGTH || !code.all { it.isDigit() } ->
                "Informe o código de $RESET_CODE_LENGTH dígitos que enviamos por e-mail."

            newPassword.length < MIN_PASSWORD_LENGTH ->
                "A senha deve ter pelo menos $MIN_PASSWORD_LENGTH caracteres."

            newPassword != confirmPassword -> "As senhas não coincidem."

            else -> null
        }

        if (problem != null) {
            _uiState.value = state.copy(errorMessage = problem)
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                accountRepository.resetPassword(state.email, code, newPassword)

                cooldownJob?.cancel()
                _uiState.value = _uiState.value.copy(isLoading = false, isReset = true)

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                // "Código inválido ou expirado.": the same answer for every wrong case.
                showError(
                    exception.apiErrorMessage() ?: "Não foi possível redefinir a senha."
                )

            } catch (exception: IOException) {
                showError("Não foi possível conectar ao servidor.")

            } catch (exception: Exception) {
                showError("Ocorreu um erro inesperado.")
            }
        }
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }

    private fun startCooldown() {
        cooldownJob?.cancel()

        cooldownJob = viewModelScope.launch {
            for (left in RESEND_COOLDOWN_SECONDS downTo 0) {
                _uiState.value = _uiState.value.copy(resendSecondsLeft = left)
                if (left > 0) delay(1_000)
            }
        }
    }
}
