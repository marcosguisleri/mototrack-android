package br.dev.guisleri.mototrack.ui.screen.motorcycles.create

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleType

data class MotorcycleCreateUiState(
    val type: MotorcycleType? = null,
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val engineCapacity: String = "",
    val color: String = "",
    val isCustomColor: Boolean = false,
    val typeError: String? = null,
    val brandError: String? = null,
    val modelError: String? = null,
    val yearError: String? = null,
    val engineCapacityError: String? = null,
    val colorError: String? = null,
    val isSubmitting: Boolean = false,
    val submitErrorMessage: String? = null,
    val isCreated: Boolean = false
)
