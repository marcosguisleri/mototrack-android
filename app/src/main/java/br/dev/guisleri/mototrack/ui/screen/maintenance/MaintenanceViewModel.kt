package br.dev.guisleri.mototrack.ui.screen.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.maintenance.ItemInterval
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceItem
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceStore
import br.dev.guisleri.mototrack.data.maintenance.MotorcycleMaintenance
import br.dev.guisleri.mototrack.data.maintenance.OdometerReading
import br.dev.guisleri.mototrack.data.maintenance.ServiceRecord
import br.dev.guisleri.mototrack.data.maintenance.currentOdometerKm
import br.dev.guisleri.mototrack.data.maintenance.intervalOf
import br.dev.guisleri.mototrack.data.maintenance.maintenanceOverview
import br.dev.guisleri.mototrack.data.maintenance.summarize
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

const val MAX_ODOMETER_DIGITS = 7
const val MAX_INTERVAL_DIGITS = 6

/**
 * The maintenance of one motorcycle. What the rider registers lives on the device;
 * the trips' kilometres come from the backend and make the odometer follow the rider.
 */
class MaintenanceViewModel(
    private val motorcycleId: Long,
    private val motorcycleRepository: MotorcycleRepository,
    private val store: MaintenanceStore,
    private val today: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceUiState())

    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            store.observe(motorcycleId).collect { maintenance ->
                _uiState.value = rebuilt(_uiState.value.copy(maintenance = maintenance))
            }
        }

        loadTrips()
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        loadTrips()
    }

    private fun loadTrips() {
        viewModelScope.launch {
            try {
                val statistics = motorcycleRepository.getMotorcycleStatistics(motorcycleId)

                _uiState.value = rebuilt(
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null,
                        motorcycleName = "${statistics.motorcycle.brand} ${statistics.motorcycle.model}",
                        tripKm = statistics.totalCompletedDistanceKm
                    )
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = when (exception) {
                        is IOException -> "Não foi possível comunicar com o servidor."
                        is HttpException -> "Não foi possível carregar a moto."
                        else -> "Não foi possível carregar a moto."
                    }
                )
            }
        }
    }

    private fun rebuilt(state: MaintenanceUiState): MaintenanceUiState {
        val overview = maintenanceOverview(state.maintenance, state.tripKm, today())

        return state.copy(
            odometerKm = currentOdometerKm(state.maintenance.odometer, state.tripKm),
            items = overview,
            summary = summarize(overview)
        )
    }

    // -- Editor -------------------------------------------------------------

    fun openOdometerEditor() {
        _uiState.value = _uiState.value.copy(
            editor = MaintenanceEditor.Odometer,
            editorErrorMessage = null
        )
    }

    fun openServiceEditor(item: MaintenanceItem) {
        _uiState.value = _uiState.value.copy(
            editor = MaintenanceEditor.Service(item),
            editorErrorMessage = null
        )
    }

    fun dismissEditor() {
        _uiState.value = _uiState.value.copy(editor = null, editorErrorMessage = null)
    }

    fun saveOdometer(kmText: String) {
        val state = _uiState.value
        val km = kmText.trim().toIntOrNull()
        val tripKm = state.tripKm

        val problem = when {
            km == null || km <= 0 -> "Informe a quilometragem do painel."
            // Without the trips' total there is nothing to count the next trips from.
            tripKm == null -> "Ainda não foi possível carregar suas viagens. Tente de novo."
            else -> null
        }

        if (problem != null || km == null || tripKm == null) {
            _uiState.value = state.copy(editorErrorMessage = problem)
            return
        }

        val reading = OdometerReading(km = km, tripKmAtReading = tripKm, date = today().toString())

        persist { it.copy(odometer = reading) }
    }

    /**
     * Registers that [item] was done on [date], at [kmText] on the odometer (blank when
     * not known), and how often to repeat it ([intervalKmText]).
     */
    fun saveService(
        item: MaintenanceItem,
        date: LocalDate,
        kmText: String,
        intervalKmText: String
    ) {
        val state = _uiState.value
        val km = kmText.trim().toIntOrNull()
        val intervalKm = intervalKmText.trim().toIntOrNull()

        val problem = when {
            kmText.isNotBlank() && (km == null || km < 0) -> "Informe uma quilometragem válida."
            date.isAfter(today()) -> "A data não pode estar no futuro."
            intervalKmText.isNotBlank() && (intervalKm == null || intervalKm <= 0) ->
                "Informe um intervalo em km maior que zero."

            intervalKm == null && item.defaultMonths == null ->
                "Informe de quantos em quantos km repetir."

            else -> null
        }

        if (problem != null) {
            _uiState.value = state.copy(editorErrorMessage = problem)
            return
        }

        val record = ServiceRecord(doneAtKm = km, date = date.toString())

        persist { current ->
            val default = ItemInterval(item.defaultKm, item.defaultMonths)
            val chosen = ItemInterval(km = intervalKm, months = item.defaultMonths)
            val customised = chosen != default

            current.copy(
                alerted = current.alerted - item.key,
                services = current.services + (item.key to record),
                intervals =
                    if (customised) {
                        current.intervals + (item.key to chosen)
                    } else {
                        current.intervals - item.key
                    }
            )
        }
    }

    fun clearService(item: MaintenanceItem) {
        persist { it.copy(services = it.services - item.key, alerted = it.alerted - item.key) }
    }

    private fun persist(change: (MotorcycleMaintenance) -> MotorcycleMaintenance) {
        viewModelScope.launch {
            try {
                store.update(motorcycleId, change)

                _uiState.value = _uiState.value.copy(editor = null, editorErrorMessage = null)

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    editorErrorMessage = "Não foi possível salvar. Tente de novo."
                )
            }
        }
    }

    /** The interval an item currently follows, to prefill its dialog. */
    fun intervalFor(item: MaintenanceItem): ItemInterval =
        intervalOf(item, _uiState.value.maintenance)
}
