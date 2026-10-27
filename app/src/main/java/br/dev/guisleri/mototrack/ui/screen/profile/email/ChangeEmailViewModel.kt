package br.dev.guisleri.mototrack.ui.screen.profile.email

import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.network.apiErrorMessage
import br.dev.guisleri.mototrack.data.repository.AccountRepository
import br.dev.guisleri.mototrack.ui.screen.auth.forgot.RESET_CODE_LENGTH
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

private const val RESEND_COOLDOWN_SECONDS = 60

/**
 * Changing the e-mail of the account: the new address and the current password, then the
 * code the server sent to the new address. When it works the server opens a new session
 * (the e-mail is part of the tokens) and the repository keeps it.
 */
class ChangeEmailViewModel(
    private val accountRepository: AccountRepository,
    private val currentEmail: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangeEmailUiState())

    val uiState: StateFlow<ChangeEmailUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    // Kept only while the flow is open, for asking another code without typing it again.
    private var currentPassword: String = ""

    fun requestCode(newEmailText: String, passwordText: String) {
        val state = _uiState.value
        if (state.isLoading) return

        val newEmail = newEmailText.trim()

        val problem = when {
            !PatternsCompat.EMAIL_ADDRESS.matcher(newEmail).matches() ->
                "Informe um e-mail válido."

            currentEmail != null && newEmail.equals(currentEmail, ignoreCase = true) ->
                "O novo e-mail é igual ao atual."

            passwordText.isEmpty() -> "Informe a sua senha atual."

            else -> null
        }

        if (problem != null) {
            _uiState.value = state.copy(errorMessage = problem)
            return
        }

        currentPassword = passwordText
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                accountRepository.requestEmailChange(newEmail, passwordText)

                _uiState.value = _uiState.value.copy(
                    step = ChangeEmailStep.CONFIRM,
                    newEmail = newEmail,
                    isLoading = false,
                    errorMessage = null
                )
                startCooldown()

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                // "Senha atual incorreta.", "Já existe um usuário...", "Aguarde um minuto...".
                showError(exception.apiErrorMessage() ?: "Não foi possível enviar o código.")

            } catch (exception: IOException) {
                showError("Não foi possível conectar ao servidor.")

            } catch (exception: Exception) {
                showError("Ocorreu um erro inesperado.")
            }
        }
    }

    fun resendCode() {
        val state = _uiState.value
        if (state.resendSecondsLeft > 0) return

        requestCode(state.newEmail, currentPassword)
    }

    /** Back to the first step, to correct the address or the password. */
    fun editRequest() {
        cooldownJob?.cancel()
        _uiState.value = ChangeEmailUiState()
    }

    fun confirm(codeText: String) {
        val state = _uiState.value
        if (state.isLoading) return

        val code = codeText.trim()

        if (code.length != RESET_CODE_LENGTH || !code.all { it.isDigit() }) {
            _uiState.value = state.copy(
                errorMessage = "Informe o código de $RESET_CODE_LENGTH dígitos que enviamos por e-mail."
            )
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                accountRepository.confirmEmailChange(code)

                cooldownJob?.cancel()
                currentPassword = ""
                _uiState.value = _uiState.value.copy(isLoading = false, isChanged = true)

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                showError(
                    exception.apiErrorMessage() ?: "Não foi possível confirmar o novo e-mail."
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

    override fun onCleared() {
        currentPassword = ""
    }
}
