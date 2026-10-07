package br.dev.guisleri.mototrack.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.repository.HomeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<HomeUiState>(
            HomeUiState.Loading
        )

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {

            _uiState.value = HomeUiState.Loading

            try {
                val home = homeRepository.getHome()

                _uiState.value =
                    HomeUiState.Success(
                        home = home
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

                _uiState.value =
                    HomeUiState.Error(message)

            } catch (exception: IOException) {

                _uiState.value =
                    HomeUiState.Error(
                        "Não foi possível comunicar com o servidor. Tente novamente."
                    )

            } catch (exception: Exception) {

                _uiState.value =
                    HomeUiState.Error(
                        "Ocorreu um erro inesperado."
                    )
            }
        }
    }
}
