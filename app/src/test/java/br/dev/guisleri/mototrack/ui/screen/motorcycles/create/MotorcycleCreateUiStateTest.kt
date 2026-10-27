package br.dev.guisleri.mototrack.ui.screen.motorcycles.create

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotorcycleCreateUiStateTest {

    private val empty = MotorcycleCreateUiState()

    @Test
    fun theEmptyFormHasNoChanges() {
        assertFalse(empty.differsFrom(empty))
    }

    @Test
    fun fillingAnyFieldCountsAsAChange() {
        assertTrue(empty.copy(brand = "Honda").differsFrom(empty))
        assertTrue(empty.copy(model = "CB 500").differsFrom(empty))
        assertTrue(empty.copy(year = "2022").differsFrom(empty))
        assertTrue(empty.copy(engineCapacity = "500").differsFrom(empty))
        assertTrue(empty.copy(color = "Azul").differsFrom(empty))
        assertTrue(empty.copy(type = MotorcycleType.NAKED).differsFrom(empty))
    }

    @Test
    fun anEditedMotorcycleIsComparedWithItsLoadedValues() {
        val loaded = empty.copy(editingMotorcycleId = 1L, brand = "Honda", model = "CB 500")

        assertFalse(loaded.differsFrom(loaded))
        assertTrue(loaded.copy(model = "CB 650").differsFrom(loaded))
    }

    @Test
    fun errorsAndLoadingFlagsAreNotChanges() {
        assertFalse(empty.copy(brandError = "x", isSubmitting = true).differsFrom(empty))
    }
}
