package br.dev.guisleri.mototrack.ui.screen.motorcycles.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository

class MotorcycleCreateViewModelFactory(
    private val motorcycleRepository: MotorcycleRepository,
    private val editingMotorcycleId: Long? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(MotorcycleCreateViewModel::class.java)) {
            return MotorcycleCreateViewModel(
                motorcycleRepository = motorcycleRepository,
                editingMotorcycleId = editingMotorcycleId
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
