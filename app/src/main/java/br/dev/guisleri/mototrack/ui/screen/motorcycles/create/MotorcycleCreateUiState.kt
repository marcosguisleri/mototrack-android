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
    val isCreated: Boolean = false,
    // Set when the form edits a registered motorcycle instead of creating one.
    val editingMotorcycleId: Long? = null,
    val isLoadingMotorcycle: Boolean = false,
    val loadErrorMessage: String? = null,
    val isUpdated: Boolean = false
) {
    val isEditing: Boolean get() = editingMotorcycleId != null

    /** Whether the user changed the form since [base]: the empty form, or the motorcycle as loaded. */
    fun differsFrom(base: MotorcycleCreateUiState): Boolean =
        type != base.type ||
            brand != base.brand ||
            model != base.model ||
            year != base.year ||
            engineCapacity != base.engineCapacity ||
            color != base.color
}
