package br.dev.guisleri.mototrack.ui.screen.motorcycles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository

class MotorcyclesViewModelFactory(
    private val motorcycleRepository: MotorcycleRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(MotorcyclesViewModel::class.java)) {
            return MotorcyclesViewModel(
                motorcycleRepository = motorcycleRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}