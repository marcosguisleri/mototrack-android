package br.dev.guisleri.mototrack.ui.screen.motorcycles.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository

class MotorcycleCreateViewModelFactory(
    private val motorcycleRepository: MotorcycleRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(MotorcycleCreateViewModel::class.java)) {
            return MotorcycleCreateViewModel(
                motorcycleRepository = motorcycleRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
