package br.dev.guisleri.mototrack.ui.screen.profile

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RiderLevelTest {

    @Test
    fun xpIsKmPlusBonusesForTripsAndDestinations() {
        // 170 km + 1 trip (50) + 1 destination (25).
        assertEquals(245, riderXp(completedTrips = 1, totalKm = 170.0, destinationCount = 1))
        assertEquals(0, riderXp(0, 0.0, 0))
    }

    @Test
    fun theLevelFollowsTheXp() {
        assertEquals("Novato", riderLevelOfXp(0).name)
        assertEquals("Novato", riderLevelOfXp(99).name)
        assertEquals("Estradeiro", riderLevelOfXp(100).name)
        assertEquals(3, riderLevelOfXp(300).level)
        assertEquals("Lenda", riderLevelOfXp(50_000).name)
    }

    @Test
    fun theProgressToTheNextLevelIsBetweenTheTwo() {
        val level = riderLevelOfXp(200) // Estradeiro 100, Viajante 300

        assertEquals("Viajante", level.nextName)
        assertEquals(100, level.xpToNext)
        assertEquals(0.5f, level.fraction, 0.001f)
    }

    @Test
    fun theTopLevelHasNothingAfterIt() {
        val level = riderLevelOfXp(20_000)

        assertTrue(level.isMax)
        assertNull(level.nextName)
        assertEquals(1f, level.fraction, 0f)
        assertEquals(0, level.xpToNext)
    }

    @Test
    fun negativeXpIsTheFirstLevel() {
        assertEquals(1, riderLevelOfXp(-5).level)
    }

    @Test
    fun onlyCompletedTripsCountAndDestinationsAreUnique() {
        val trips = listOf(
            trip(1, "Araras", "Leme", 21.0, "COMPLETED"),
            trip(2, "Leme", "Leme", 10.0, "COMPLETED"),
            trip(3, "Araras", "Santos", 500.0, "PLANNED"),
            trip(4, "Araras", "Rio Claro", 30.0, "IN_PROGRESS")
        )

        val level = riderLevelOf(trips)

        // 31 km + 2 trips (100) + 1 destination (25).
        assertEquals(156, level.xp)
        assertEquals("Estradeiro", level.name)
    }

    @Test
    fun noTripsIsTheFirstLevelWithNoXp() {
        assertEquals(0, riderLevelOf(emptyList<TripResponseDTO>()).xp)
    }

    private fun trip(id: Long, origin: String, destination: String, km: Double, status: String) =
        TripResponseDTO(
            id = id, origin = origin, destination = destination, distanceKm = km,
            status = status, terrain = "ASPHALT", tripDate = "2026-10-20",
            motorcycle = MotorcycleResponseDTO(
                id = 1, brand = "Honda", model = "CB", color = "Preta", year = 2024,
                engineCapacity = 500, owner = MotorcycleOwnerDTO(1, "Rider", "rider@example.com")
            )
        )
}
