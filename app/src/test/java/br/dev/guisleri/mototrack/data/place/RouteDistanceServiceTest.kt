package br.dev.guisleri.mototrack.data.place

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RouteDistanceServiceTest {

    private fun service(
        geocoding: FakeGeocodingApi = FakeGeocodingApi(),
        ibge: FakeIbgeApi = FakeIbgeApi(),
        osrm: FakeOsrmApi = FakeOsrmApi()
    ) = RouteDistanceService(geocoding, ibge, osrm)

    @Test
    fun theRoadDistanceComesFromTheRoutingService() = runTest {
        val osrm = FakeOsrmApi(kilometres = 21.94)

        val result = service(osrm = osrm).distanceBetween(ARARAS, LEME)

        assertEquals(RouteDistance(21.9, approximate = false), result)
        // Longitude first, as the routing service wants it, with the right Araras (São Paulo).
        assertEquals("-47.38417,-22.35694;-47.38972,-22.18111", osrm.requests.single())
    }

    @Test
    fun theCityWithTheSameNameInAnotherStateIsNotTheOneUsed() = runTest {
        val osrm = FakeOsrmApi()

        service(osrm = osrm).distanceBetween(ARARAS, LEME)

        assertFalse(osrm.requests.single().contains("-3.78333"))
    }

    @Test
    fun withoutARouteTheStraightLineScaledToRoadsStandsIn() = runTest {
        val osrm = FakeOsrmApi().apply { failure = IOException("busy") }

        val result = service(osrm = osrm).distanceBetween(ARARAS, LEME)!!

        assertTrue(result.approximate)
        // About 19.6 km in a straight line, times 1.25.
        assertEquals(24.5, result.km, 1.0)
    }

    @Test
    fun theMunicipalityCentreIsUsedWhenTheCityIsNotFound() = runTest {
        val geocoding = FakeGeocodingApi(results = emptyMap())
        val ibge = FakeIbgeApi(
            centres = mapOf(
                ARARAS.id to IbgeCentroidDTO(-22.3462, -47.3239),
                LEME.id to IbgeCentroidDTO(-22.18, -47.39)
            )
        )
        val osrm = FakeOsrmApi()

        val result = service(geocoding, ibge, osrm).distanceBetween(ARARAS, LEME)

        assertNotNull(result)
        assertTrue(osrm.requests.single().startsWith("-47.3239,-22.3462;"))
    }

    @Test
    fun nothingIsCalculatedWhenACityCannotBePlaced() = runTest {
        val geocoding = FakeGeocodingApi().apply { failure = IOException("offline") }
        val osrm = FakeOsrmApi()

        // No coordinates anywhere: no connection at all.
        assertNull(service(geocoding, FakeIbgeApi(), osrm).distanceBetween(ARARAS, LEME))
        assertTrue(osrm.requests.isEmpty())
    }

    @Test
    fun aCityIsLocatedOnlyOnce() = runTest {
        val geocoding = FakeGeocodingApi()
        val service = service(geocoding = geocoding)

        service.distanceBetween(ARARAS, LEME)
        service.distanceBetween(ARARAS, LEME)

        assertEquals(2, geocoding.searches)
    }

    @Test
    fun aCityIsNotMeasuredToItself() = runTest {
        assertNull(service().distanceBetween(ARARAS, ARARAS))
    }

    @Test
    fun theStraightLineOfKnownPlaces() {
        // São Paulo to Rio de Janeiro is about 360 km as the crow flies.
        val saoPaulo = Coordinates(-23.5505, -46.6333)
        val rio = Coordinates(-22.9068, -43.1729)

        assertEquals(360.0, straightLineKm(saoPaulo, rio), 10.0)
        assertEquals(0.0, straightLineKm(saoPaulo, saoPaulo), 0.001)
    }
}
