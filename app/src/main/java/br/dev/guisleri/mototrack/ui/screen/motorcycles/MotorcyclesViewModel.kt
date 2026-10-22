package br.dev.guisleri.mototrack.ui.screen.motorcycles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.local.MotorcyclePreferences
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository
import br.dev.guisleri.mototrack.ui.feedback.Feedback
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MotorcyclesViewModel(
    private val motorcycleRepository: MotorcycleRepository,
    private val motorcyclePreferences: MotorcyclePreferences
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<MotorcyclesUiState>(
            MotorcyclesUiState.Loading
        )

    val uiState: StateFlow<MotorcyclesUiState> =
        _uiState.asStateFlow()

    // One-off confirmations; buffered so none is lost while nobody is collecting.
    private val _feedback = Channel<Feedback>(Channel.BUFFERED)

    val feedback: Flow<Feedback> = _feedback.receiveAsFlow()

    private var motorcyclesJob: Job? = null
    private var statisticsJob: Job? = null

    init {
        loadMotorcycles()
    }

    fun loadMotorcycles() {
        val previouslySelectedId =
            (_uiState.value as? MotorcyclesUiState.Success)?.selectedMotorcycleId
        motorcyclesJob?.cancel()
        statisticsJob?.cancel()
        _uiState.value = MotorcyclesUiState.Loading

        motorcyclesJob = viewModelScope.launch {
            try {
                val motorcycles =
                    motorcycleRepository.getMotorcycles()

                if (motorcycles.isEmpty()) {
                    _uiState.value =
                        MotorcyclesUiState.Empty

                    return@launch
                }

                // Losing the preference must never break the screen.
                val defaultMotorcycleId =
                    runCatching { motorcyclePreferences.defaultMotorcycleId.first() }
                        .getOrNull()
                        ?.takeIf { defaultId ->
                            motorcycles.any { it.id == defaultId }
                        }

                val selectedMotorcycleId =
                    previouslySelectedId?.takeIf { selectedId ->
                        motorcycles.any { it.id == selectedId }
                    } ?: defaultMotorcycleId
                    ?: motorcycles.first().id

                _uiState.value =
                    MotorcyclesUiState.Success(
                        motorcycles = motorcycles,
                        selectedMotorcycleId = selectedMotorcycleId,
                        defaultMotorcycleId = defaultMotorcycleId,
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

        statisticsJob?.cancel()
        loadMotorcycleStatistics(
            motorcycleId = motorcycleId
        )
    }

    fun deleteMotorcycle(
        motorcycleId: Long
    ) {
        val currentState = _uiState.value as? MotorcyclesUiState.Success
            ?: return

        if (currentState.isDeleting) return

        val deletedName = currentState.motorcycles
            .firstOrNull { it.id == motorcycleId }
            ?.let { "${it.brand} ${it.model}" }

        _uiState.value = currentState.copy(
            isDeleting = true,
            deleteIssue = null
        )

        viewModelScope.launch {
            try {
                motorcycleRepository.deleteMotorcycle(motorcycleId)

                _feedback.trySend(
                    Feedback(
                        if (deletedName != null) "Moto excluída: $deletedName" else "Moto excluída"
                    )
                )

                loadMotorcycles()

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                showDeleteIssue(
                    if (exception.code() == 409) {
                        MotorcycleDeleteIssue.HasTrips
                    } else {
                        MotorcycleDeleteIssue.Failed(
                            "Não foi possível excluir a moto."
                        )
                    }
                )

            } catch (exception: IOException) {
                showDeleteIssue(
                    MotorcycleDeleteIssue.Failed(
                        "Não foi possível comunicar com o servidor."
                    )
                )

            } catch (exception: Exception) {
                showDeleteIssue(
                    MotorcycleDeleteIssue.Failed(
                        "Não foi possível excluir a moto."
                    )
                )
            }
        }
    }

    fun dismissDeleteIssue() {
        val currentState = _uiState.value as? MotorcyclesUiState.Success
            ?: return

        _uiState.value = currentState.copy(deleteIssue = null)
    }

    private fun showDeleteIssue(issue: MotorcycleDeleteIssue) {
        val currentState = _uiState.value as? MotorcyclesUiState.Success
            ?: return

        _uiState.value = currentState.copy(
            isDeleting = false,
            deleteIssue = issue
        )
    }

    fun setDefaultMotorcycle(
        motorcycleId: Long
    ) {
        val currentState = _uiState.value as? MotorcyclesUiState.Success
            ?: return

        if (
            currentState.defaultMotorcycleId == motorcycleId ||
            currentState.motorcycles.none { it.id == motorcycleId }
        ) {
            return
        }

        _uiState.value = currentState.copy(
            defaultMotorcycleId = motorcycleId
        )

        currentState.motorcycles.firstOrNull { it.id == motorcycleId }?.let { motorcycle ->
            _feedback.trySend(
                Feedback("Moto padrão: ${motorcycle.brand} ${motorcycle.model}")
            )
        }

        viewModelScope.launch {
            try {
                motorcyclePreferences.setDefaultMotorcycleId(motorcycleId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                // Keep the choice for this session even if it could not be saved.
            }
        }
    }

    fun retrySelectedStatistics() {
        val currentState = _uiState.value as? MotorcyclesUiState.Success
            ?: return
        _uiState.value = currentState.copy(
            statisticsUiState = MotorcycleStatisticsUiState.Loading
        )
        statisticsJob?.cancel()
        loadMotorcycleStatistics(currentState.selectedMotorcycleId)
    }

    private fun loadMotorcycleStatistics(
        motorcycleId: Long
    ) {
        statisticsJob = viewModelScope.launch {
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