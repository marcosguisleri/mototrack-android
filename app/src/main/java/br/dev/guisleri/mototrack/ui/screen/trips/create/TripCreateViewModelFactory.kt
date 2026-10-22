package br.dev.guisleri.mototrack.ui.screen.trips.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.dev.guisleri.mototrack.data.local.MotorcyclePreferences
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository
import br.dev.guisleri.mototrack.data.repository.TripRepository

class TripCreateViewModelFactory(
    private val tripRepository: TripRepository,
    private val motorcycleRepository: MotorcycleRepository,
    private val motorcyclePreferences: MotorcyclePreferences,
    private val initialMode: TripCreateMode = TripCreateMode.PLANNED,
    private val requestedMotorcycleId: Long? = null,
    private val editingTripId: Long? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(TripCreateViewModel::class.java)) {
            return TripCreateViewModel(
                tripRepository = tripRepository,
                motorcycleRepository = motorcycleRepository,
                motorcyclePreferences = motorcyclePreferences,
                initialMode = initialMode,
                requestedMotorcycleId = requestedMotorcycleId,
                editingTripId = editingTripId
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
