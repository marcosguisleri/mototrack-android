package br.dev.guisleri.mototrack.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import br.dev.guisleri.mototrack.data.repository.HomeRepository
import br.dev.guisleri.mototrack.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // The name is fetched only once, and replaced when the rider edits it.
    private var userName: String? = null

    private val _uiState =
        MutableStateFlow<HomeUiState>(
            HomeUiState.Loading
        )

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadHome()
    }

    /** The rider changed their name: the greeting follows without reloading the Home. */
    fun onUserNameChanged(name: String) {
        userName = name

        (_uiState.value as? HomeUiState.Success)?.let {
            _uiState.value = it.copy(userName = name)
        }
    }

    fun loadHome() {
        loadJob?.cancel()
        val previousSuccess = _uiState.value as? HomeUiState.Success
        if (previousSuccess == null) {
            _uiState.value = HomeUiState.Loading
        } else {
            _uiState.value = previousSuccess.copy(
                isRefreshing = true,
                refreshErrorMessage = null
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val home = homeRepository.getHome()

                _uiState.value = HomeUiState.Success(
                    home = home,
                    highlight = resolveHighlight(home),
                    userName = userName ?: loadUserName()
                )
            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                val message =
                    when (exception.code()) {
                        401 -> "Sua sessão não é mais válida."
                        403 -> "Você não tem permissão para acessar esses dados."
                        else -> "Não foi possível carregar a Home."
                    }

                showLoadError(previousSuccess, message)

            } catch (exception: IOException) {
                showLoadError(
                    previousSuccess,
                    "Não foi possível comunicar com o servidor. Tente novamente."
                )

            } catch (exception: Exception) {
                showLoadError(previousSuccess, "Ocorreu um erro inesperado.")
            }
        }
    }

    /** The greeting works without a name, so any failure here is ignored. */
    private suspend fun loadUserName(): String? =
        try {
            userRepository.getCurrentUser().name.also { userName = it }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            null
        }

    private fun showLoadError(
        previousSuccess: HomeUiState.Success?,
        message: String
    ) {
        _uiState.value = previousSuccess?.copy(
            isRefreshing = false,
            refreshErrorMessage = message
        ) ?: HomeUiState.Error(message)
    }

    private fun resolveHighlight(
        home: HomeResponseDTO
    ): HomeHighlightState {

        home.inProgressTrip?.let { trip ->
            return HomeHighlightState.InProgress(
                trip = trip
            )
        }

        if (home.motorcycleCount == 0L) {
            return HomeHighlightState.NoMotorcycle
        }

        home.nextTrip?.let { trip ->
            return HomeHighlightState.UpcomingTrip(
                trip = trip
            )
        }

        return HomeHighlightState.NoUpcomingTrip
    }
}
