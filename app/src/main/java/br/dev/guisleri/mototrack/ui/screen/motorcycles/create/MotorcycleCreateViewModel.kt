package br.dev.guisleri.mototrack.ui.screen.motorcycles.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.model.common.ApiErrorResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.CreateMotorcycleRequestDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleType
import br.dev.guisleri.mototrack.data.model.motorcycle.UpdateMotorcycleRequestDTO
import br.dev.guisleri.mototrack.ui.components.MotorcycleColorOptions
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

private const val MAX_TEXT_LENGTH = 255
private const val MAX_YEAR_DIGITS = 4
private const val MAX_ENGINE_DIGITS = 4
private const val MIN_YEAR = 1900

private val apiErrorJson = Json {
    ignoreUnknownKeys = true
}

class MotorcycleCreateViewModel(
    private val motorcycleRepository: MotorcycleRepository,
    private val editingMotorcycleId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MotorcycleCreateUiState(
            editingMotorcycleId = editingMotorcycleId,
            isLoadingMotorcycle = editingMotorcycleId != null
        )
    )

    val uiState: StateFlow<MotorcycleCreateUiState> = _uiState.asStateFlow()

    init {
        loadMotorcycle()
    }

    /** When editing, fills the form with the motorcycle as the server has it. */
    fun loadMotorcycle() {
        val motorcycleId = editingMotorcycleId ?: return

        _uiState.value = _uiState.value.copy(
            isLoadingMotorcycle = true,
            loadErrorMessage = null
        )

        viewModelScope.launch {
            try {
                val motorcycle = motorcycleRepository.getMotorcycles()
                    .firstOrNull { it.id == motorcycleId }

                if (motorcycle == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMotorcycle = false,
                        loadErrorMessage = "Não encontramos esta moto na sua garagem."
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoadingMotorcycle = false,
                    type = MotorcycleType.fromApi(motorcycle.type),
                    brand = motorcycle.brand,
                    model = motorcycle.model,
                    year = motorcycle.year.toString(),
                    engineCapacity = motorcycle.engineCapacity.toString(),
                    color = motorcycle.color,
                    // A colour outside the swatches was typed by the rider.
                    isCustomColor = MotorcycleColorOptions.none { it.first == motorcycle.color }
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMotorcycle = false,
                    loadErrorMessage = "Não foi possível comunicar com o servidor."
                )

            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMotorcycle = false,
                    loadErrorMessage = "Não foi possível carregar a moto."
                )
            }
        }
    }

    fun onTypeSelected(type: MotorcycleType) {
        _uiState.value = _uiState.value.copy(
            type = type,
            typeError = null,
            submitErrorMessage = null
        )
    }

    fun onBrandChange(brand: String) {
        _uiState.value = _uiState.value.copy(
            brand = brand.take(MAX_TEXT_LENGTH),
            brandError = null,
            submitErrorMessage = null
        )
    }

    fun onModelChange(model: String) {
        _uiState.value = _uiState.value.copy(
            model = model.take(MAX_TEXT_LENGTH),
            modelError = null,
            submitErrorMessage = null
        )
    }

    fun onYearChange(year: String) {
        _uiState.value = _uiState.value.copy(
            year = year.filter { it.isDigit() }.take(MAX_YEAR_DIGITS),
            yearError = null,
            submitErrorMessage = null
        )
    }

    fun onEngineCapacityChange(engineCapacity: String) {
        _uiState.value = _uiState.value.copy(
            engineCapacity = engineCapacity.filter { it.isDigit() }.take(MAX_ENGINE_DIGITS),
            engineCapacityError = null,
            submitErrorMessage = null
        )
    }

    fun onColorSelected(color: String) {
        _uiState.value = _uiState.value.copy(
            color = color,
            isCustomColor = false,
            colorError = null,
            submitErrorMessage = null
        )
    }

    fun onCustomColorSelected() {
        val state = _uiState.value
        if (state.isCustomColor) return

        _uiState.value = state.copy(
            color = "",
            isCustomColor = true,
            colorError = null,
            submitErrorMessage = null
        )
    }

    fun onCustomColorChange(color: String) {
        _uiState.value = _uiState.value.copy(
            color = color.take(MAX_TEXT_LENGTH),
            colorError = null,
            submitErrorMessage = null
        )
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting || state.isLoadingMotorcycle || state.loadErrorMessage != null) {
            return
        }

        val brand = state.brand.trim()
        val model = state.model.trim()
        val color = state.color.trim()
        val year = state.year.toIntOrNull()
        val engineCapacity = state.engineCapacity.toIntOrNull()
        val maxYear = LocalDate.now().year + 1

        val type = state.type
        val typeError = "Escolha o tipo da moto.".takeIf { type == null }
        val brandError = "Informe a marca.".takeIf { brand.isEmpty() }
        val modelError = "Informe o modelo.".takeIf { model.isEmpty() }
        val yearError = "Informe um ano entre $MIN_YEAR e $maxYear."
            .takeIf { year == null || year < MIN_YEAR || year > maxYear }
        val engineCapacityError = "Informe a cilindrada."
            .takeIf { engineCapacity == null || engineCapacity <= 0 }
        val colorError = "Escolha a cor da moto.".takeIf { color.isEmpty() }

        if (
            typeError != null ||
            brandError != null ||
            modelError != null ||
            yearError != null ||
            engineCapacityError != null ||
            colorError != null ||
            type == null ||
            year == null ||
            engineCapacity == null
        ) {
            _uiState.value = state.copy(
                typeError = typeError,
                brandError = brandError,
                modelError = modelError,
                yearError = yearError,
                engineCapacityError = engineCapacityError,
                colorError = colorError
            )
            return
        }

        _uiState.value = state.copy(
            isSubmitting = true,
            submitErrorMessage = null
        )

        viewModelScope.launch {
            try {
                if (editingMotorcycleId != null) {
                    motorcycleRepository.updateMotorcycle(
                        motorcycleId = editingMotorcycleId,
                        request = UpdateMotorcycleRequestDTO(
                            brand = brand,
                            model = model,
                            color = color,
                            year = year,
                            engineCapacity = engineCapacity,
                            type = type.apiValue
                        )
                    )
                } else {
                    motorcycleRepository.createMotorcycle(
                        CreateMotorcycleRequestDTO(
                            brand = brand,
                            model = model,
                            color = color,
                            year = year,
                            engineCapacity = engineCapacity,
                            type = type.apiValue
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    isCreated = editingMotorcycleId == null,
                    isUpdated = editingMotorcycleId != null
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitErrorMessage =
                        getApiErrorMessage(exception)
                            ?: failureText()
                )

            } catch (exception: IOException) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitErrorMessage = "Não foi possível comunicar com o servidor."
                )

            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitErrorMessage = failureText()
                )
            }
        }
    }

    private fun failureText(): String =
        if (editingMotorcycleId != null) {
            "Não foi possível salvar as alterações."
        } else {
            "Não foi possível cadastrar a moto."
        }

    private fun getApiErrorMessage(
        exception: HttpException
    ): String? {
        val body =
            exception.response()
                ?.errorBody()
                ?.string()
                ?: return null

        return runCatching {
            apiErrorJson.decodeFromString<ApiErrorResponseDTO>(
                body
            ).message
        }.getOrNull()
    }
}
