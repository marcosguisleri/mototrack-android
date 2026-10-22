package br.dev.guisleri.mototrack.ui.screen.trips.details

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripRouteMapLayoutTest {

    private val seeds = (1L..200L).map { routeSeed("Cidade $it", "Destino ${it * 7}") }

    @Test
    fun theSamePlacesAlwaysGiveTheSameMap() {
        assertEquals(
            routeMapLayout(routeSeed("Araras", "Leme")),
            routeMapLayout(routeSeed("Araras", "Leme"))
        )
    }

    @Test
    fun theSeedIgnoresCaseAndSurroundingSpaces() {
        assertEquals(routeSeed("Araras", "Leme"), routeSeed("  ARARAS ", "leme  "))
    }

    @Test
    fun differentPlacesGiveDifferentMaps() {
        assertNotEquals(
            routeMapLayout(routeSeed("Araras", "Leme")),
            routeMapLayout(routeSeed("Santos", "Campinas"))
        )
    }

    @Test
    fun theRouteRunsOverLandInsideTheMap() {
        seeds.forEach { seed ->
            val layout = routeMapLayout(seed)

            assertTrue(layout.route.size >= 5)
            layout.route.forEach { point ->
                assertTrue("$point out of the map", point.x in 0.05f..0.95f && point.y in 0.1f..0.9f)

                // Land is on the opposite side of the sea, with room before the shore.
                val shore = layout.coast.minOf { if (layout.seaOnRight) it.x else 1f - it.x }
                val distanceFromSea = if (layout.seaOnRight) shore - point.x else point.x - (1f - shore)
                assertTrue("$point too close to the sea", distanceFromSea > 0.05f)
            }
        }
    }

    @Test
    fun theTripGoesFromTheBottomOfTheMapToItsTop() {
        seeds.forEach { seed ->
            val layout = routeMapLayout(seed)

            assertTrue(layout.origin.y > layout.destination.y)
        }
    }

    @Test
    fun bothSidesOfTheSeeAppear() {
        val sides = seeds.map { routeMapLayout(it).seaOnRight }.toSet()

        assertEquals(setOf(true, false), sides)
    }
}
