package br.dev.guisleri.mototrack.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.repository.AuthRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<LoginUiState>(
            LoginUiState.Idle
        )

    val uiState: StateFlow<LoginUiState> =
        _uiState.asStateFlow()

    fun login(
        email: String,
        password: String
    ) {
        if (
            email.isBlank() ||
            password.isBlank()
        ) {
            _uiState.value =
                LoginUiState.Error(
                    "Preencha o e-mail e a senha."
                )

            return
        }

        viewModelScope.launch {

            _uiState.value =
                LoginUiState.Loading

            try {
                val tokens =
                    authRepository.login(
                        email = email.trim(),
                        password = password
                    )

                _uiState.value =
                    LoginUiState.Success(
                        tokens
                    )

            } catch (exception: HttpException) {

                val message =
                    when (exception.code()) {

                        401 ->
                            "E-mail ou senha inválidos."

                        else ->
                            "Não foi possível realizar o login."
                    }

                _uiState.value =
                    LoginUiState.Error(
                        message
                    )

            } catch (exception: IOException) {
                exception.printStackTrace()

                _uiState.value = LoginUiState.Error(
                    "${exception::class.simpleName}: ${exception.message}"
                )
            } catch (exception: Exception) {

                _uiState.value =
                    LoginUiState.Error(
                        "Ocorreu um erro inesperado."
                    )
            }
        }
    }
}