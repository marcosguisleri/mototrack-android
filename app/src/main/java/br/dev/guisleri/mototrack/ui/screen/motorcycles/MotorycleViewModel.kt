package br.dev.guisleri.mototrack.ui.screen.motorcycles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class MotorcyclesViewModel(
    private val motorcycleRepository: MotorcycleRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<MotorcyclesUiState>(
            MotorcyclesUiState.Loading
        )

    val uiState: StateFlow<MotorcyclesUiState> =
        _uiState.asStateFlow()

    init {
        loadMotorcycles()
    }

    fun loadMotorcycles() {
        _uiState.value = MotorcyclesUiState.Loading

        viewModelScope.launch {
            try {
                val motorcycles =
                    motorcycleRepository.getMotorcycles()

                if (motorcycles.isEmpty()) {
                    _uiState.value =
                        MotorcyclesUiState.Empty

                    return@launch
                }

                val selectedMotorcycleId =
                    motorcycles.first().id

                _uiState.value =
                    MotorcyclesUiState.Success(
                        motorcycles = motorcycles,
                        selectedMotorcycleId = selectedMotorcycleId,
                        statisticsUiState =
                            MotorcycleStatisticsUiState.Loading
                    )

                loadMotorcycleStatistics(
                    motorcycleId = selectedMotorcycleId
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                _uiState.value =
                    MotorcyclesUiState.Error(
                        message = "Não foi possível comunicar com o servidor."
                    )

            } catch (exception: Exception) {
                _uiState.value =
                    MotorcyclesUiState.Error(
                        message = "Não foi possível carregar suas motos."
                    )
            }
        }
    }

    fun selectMotorcycle(
        motorcycleId: Long
    ) {
        val currentState = _uiState.value

        if (currentState !is MotorcyclesUiState.Success) {
            return
        }

        if (
            currentState.motorcycles.none {
                it.id == motorcycleId
            }
        ) {
            return
        }

        if (currentState.selectedMotorcycleId == motorcycleId) {
            return
        }

        _uiState.value =
            currentState.copy(
                selectedMotorcycleId = motorcycleId,
                statisticsUiState =
                    MotorcycleStatisticsUiState.Loading
            )

        loadMotorcycleStatistics(
            motorcycleId = motorcycleId
        )
    }

    private fun loadMotorcycleStatistics(
        motorcycleId: Long
    ) {
        viewModelScope.launch {
            try {
                val statistics =
                    motorcycleRepository.getMotorcycleStatistics(
                        motorcycleId = motorcycleId
                    )

                val currentState = _uiState.value

                if (
                    currentState is MotorcyclesUiState.Success &&
                    currentState.selectedMotorcycleId == motorcycleId
                ) {
                    _uiState.value =
                        currentState.copy(
                            statisticsUiState =
                                MotorcycleStatisticsUiState.Success(
                                    statistics = statistics
                                )
                        )
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                updateStatisticsError(
                    motorcycleId = motorcycleId,
                    message = "Não foi possível carregar as estatísticas."
                )

            } catch (exception: Exception) {
                updateStatisticsError(
                    motorcycleId = motorcycleId,
                    message = "Não foi possível carregar as estatísticas."
                )
            }
        }
    }

    private fun updateStatisticsError(
        motorcycleId: Long,
        message: String
    ) {
        val currentState = _uiState.value

        if (
            currentState is MotorcyclesUiState.Success &&
            currentState.selectedMotorcycleId == motorcycleId
        ) {
            _uiState.value =
                currentState.copy(
                    statisticsUiState =
                        MotorcycleStatisticsUiState.Error(
                            message = message
                        )
                )
        }
    }
}