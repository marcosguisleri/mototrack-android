package br.dev.guisleri.mototrack.ui.screen.maintenance

import br.dev.guisleri.mototrack.data.maintenance.MaintenanceItem
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceProgress
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceSummary
import br.dev.guisleri.mototrack.data.maintenance.MotorcycleMaintenance

/** What the rider is filling in, while its dialog is open. */
sealed interface MaintenanceEditor {

    data object Odometer : MaintenanceEditor

    data class Service(
        val item: MaintenanceItem
    ) : MaintenanceEditor
}

data class MaintenanceUiState(
    val isLoading: Boolean = true,
    // Set when the motorcycle's trips could not be loaded; the odometer needs them.
    val errorMessage: String? = null,
    val motorcycleName: String = "",
    // Kilometres of completed trips, from the backend; null until loaded.
    val tripKm: Double? = null,
    val maintenance: MotorcycleMaintenance = MotorcycleMaintenance(),
    val odometerKm: Int? = null,
    val items: List<MaintenanceProgress> = emptyList(),
    val summary: MaintenanceSummary = MaintenanceSummary(0, 0, 0),
    val editor: MaintenanceEditor? = null,
    val editorErrorMessage: String? = null
)
