package br.dev.guisleri.mototrack.ui.screen.trips.details.costs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.dev.guisleri.mototrack.data.offline.OfflineServices
import br.dev.guisleri.mototrack.data.repository.TripCostRepository

class TripCostsViewModelFactory(
    private val tripId: Long,
    private val repository: TripCostRepository,
    private val offline: OfflineServices? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(TripCostsViewModel::class.java)) {
            return TripCostsViewModel(
                tripId = tripId,
                repository = repository,
                pendingStore = offline?.store,
                syncer = offline?.syncer
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
