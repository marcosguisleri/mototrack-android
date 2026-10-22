package br.dev.guisleri.mototrack.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())

    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadUser()
    }

    fun loadUser() {
        if (loadJob?.isActive == true) return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        loadJob = viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState(
                    user = userRepository.getCurrentUser(),
                    isLoading = false
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: HttpException) {
                showError("Não foi possível carregar seus dados.")
            } catch (exception: IOException) {
                showError("Não foi possível comunicar com o servidor.")
            } catch (exception: Exception) {
                showError("Ocorreu um erro inesperado.")
            }
        }
    }

    // A previously loaded user stays on screen; only the message is added.
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }
}
