package br.dev.guisleri.mototrack.ui.screen.trips.create

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TripCreateUiStateTest {

    private val empty = TripCreateUiState()

    @Test
    fun theEmptyFormHasNoChanges() {
        assertFalse(empty.differsFrom(empty))
    }

    @Test
    fun typingARouteCountsAsAChange() {
        assertTrue(empty.copy(origin = "São Paulo, SP").differsFrom(empty))
        assertTrue(empty.copy(destination = "Santos, SP").differsFrom(empty))
        assertTrue(empty.copy(distance = "80").differsFrom(empty))
    }

    @Test
    fun changingTheDateOrTerrainCountsAsAChange() {
        assertTrue(empty.copy(tripDate = LocalDate.now().plusDays(1)).differsFrom(empty))
        assertTrue(empty.copy(terrain = TripCreateTerrain.OFF_ROAD).differsFrom(empty))
    }

    @Test
    fun theMotorcycleChosenForTheRiderIsNotAChangeWhenCreating() {
        assertFalse(empty.copy(selectedMotorcycleId = 3L).differsFrom(empty))
    }

    @Test
    fun theMotorcycleIsAChangeWhenEditing() {
        val loaded = empty.copy(editingTripId = 1L, origin = "A", selectedMotorcycleId = 3L)

        assertFalse(loaded.differsFrom(loaded))
        assertTrue(loaded.copy(selectedMotorcycleId = 4L).differsFrom(loaded))
    }

    @Test
    fun undoingAnEditIsNoLongerAChange() {
        val loaded = empty.copy(editingTripId = 1L, origin = "A", distance = "10")

        assertTrue(loaded.copy(distance = "12").differsFrom(loaded))
        assertFalse(loaded.copy(distance = "12").copy(distance = "10").differsFrom(loaded))
    }
}
