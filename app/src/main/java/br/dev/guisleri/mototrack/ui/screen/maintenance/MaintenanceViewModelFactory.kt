package br.dev.guisleri.mototrack.ui.screen.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceStore
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository

class MaintenanceViewModelFactory(
    private val motorcycleId: Long,
    private val motorcycleRepository: MotorcycleRepository,
    private val store: MaintenanceStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(MaintenanceViewModel::class.java)) {
            return MaintenanceViewModel(
                motorcycleId = motorcycleId,
                motorcycleRepository = motorcycleRepository,
                store = store
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
