package br.dev.guisleri.mototrack.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.network.apiErrorMessage
import br.dev.guisleri.mototrack.data.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
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

    // Told when the name was saved, so the screens that show it can follow.
    private val _nameUpdated = Channel<String>(Channel.BUFFERED)

    val nameUpdated: Flow<String> = _nameUpdated.receiveAsFlow()

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

    fun openNameEditor() {
        if (_uiState.value.user == null) return

        _uiState.value = _uiState.value.copy(isEditingName = true, nameErrorMessage = null)
    }

    fun dismissNameEditor() {
        if (_uiState.value.isSavingName) return

        _uiState.value = _uiState.value.copy(isEditingName = false, nameErrorMessage = null)
    }

    fun saveName(text: String) {
        val state = _uiState.value
        val user = state.user ?: return
        if (state.isSavingName) return

        val name = text.trim()

        when {
            name.isEmpty() -> {
                _uiState.value = state.copy(nameErrorMessage = "Informe seu nome.")
                return
            }

            name == user.name -> {
                // Nothing changed: no need to ask the server.
                dismissNameEditor()
                return
            }
        }

        _uiState.value = state.copy(isSavingName = true, nameErrorMessage = null)

        viewModelScope.launch {
            try {
                val updated = userRepository.updateName(name)

                _uiState.value = _uiState.value.copy(
                    user = updated,
                    isEditingName = false,
                    isSavingName = false,
                    nameErrorMessage = null
                )
                _nameUpdated.trySend(updated.name)

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                showNameError(
                    exception.apiErrorMessage() ?: "Não foi possível salvar o nome."
                )

            } catch (exception: IOException) {
                showNameError("Não foi possível comunicar com o servidor.")

            } catch (exception: Exception) {
                showNameError("Não foi possível salvar o nome.")
            }
        }
    }

    private fun showNameError(message: String) {
        _uiState.value = _uiState.value.copy(isSavingName = false, nameErrorMessage = message)
    }

    // A previously loaded user stays on screen; only the message is added.
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }
}
