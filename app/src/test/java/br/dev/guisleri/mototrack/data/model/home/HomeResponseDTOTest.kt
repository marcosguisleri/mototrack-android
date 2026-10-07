package br.dev.guisleri.mototrack.data.model.home

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeResponseDTOTest {
    @Test
    fun decodesBackendCountersBeyondIntRange() {
        val home = Json.decodeFromString<HomeResponseDTO>(
            """{
                "nextTrip": null,
                "lastCompletedTrip": null,
                "totalCompletedDistanceKm": 42.5,
                "completedTrips": 2147483648,
                "motorcycleCount": 2147483649
            }"""
        )

        assertEquals(2147483648L, home.completedTrips)
        assertEquals(2147483649L, home.motorcycleCount)
    }
}
