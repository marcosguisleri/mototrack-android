package br.dev.guisleri.mototrack.ui.screen.auth.register

import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<RegisterUiState>(
            RegisterUiState.Idle
        )

    val uiState: StateFlow<RegisterUiState> =
        _uiState.asStateFlow()

    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (_uiState.value is RegisterUiState.Loading) {
            return
        }

        if (
            name.isBlank() ||
            email.isBlank() ||
            password.isBlank() ||
            confirmPassword.isBlank()
        ) {
            _uiState.value =
                RegisterUiState.Error("Preencha todos os campos.")
            return
        }

        if (password.length < 8) {
            _uiState.value =
                RegisterUiState.Error(
                    "A senha deve ter pelo menos 8 caracteres."
                )
            return
        }

        if (password != confirmPassword) {
            _uiState.value =
                RegisterUiState.Error("As senhas não coincidem.")
            return
        }

        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value =
                RegisterUiState.Error("Informe um e-mail válido.")
            return
        }

        _uiState.value = RegisterUiState.Loading

        viewModelScope.launch {
            try {
                authRepository.register(
                    name = name.trim(),
                    email = email.trim(),
                    password = password
                )

                _uiState.value = RegisterUiState.Success

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                _uiState.value = when (exception.code()) {
                    409 -> RegisterUiState.Error(
                        "Já existe um usuário cadastrado com este e-mail."
                    )

                    400 -> RegisterUiState.Error(
                        "Verifique os dados informados."
                    )

                    else -> RegisterUiState.Error(
                        "Não foi possível realizar o cadastro."
                    )
                }

            } catch (exception: IOException) {
                _uiState.value =
                    RegisterUiState.Error(
                        "Não foi possível conectar ao servidor."
                    )

            } catch (exception: Exception) {
                _uiState.value =
                    RegisterUiState.Error(
                        "Ocorreu um erro inesperado."
                    )
            }
        }
    }
}
