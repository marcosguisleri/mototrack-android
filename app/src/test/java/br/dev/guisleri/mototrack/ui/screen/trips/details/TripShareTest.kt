package br.dev.guisleri.mototrack.ui.screen.trips.details

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripShareTest {

    private fun trip(status: String) = TripResponseDTO(
        id = 1, origin = "São Paulo", destination = "Santos", distanceKm = 70.0,
        status = status, terrain = "ASPHALT", tripDate = "2026-10-19",
        motorcycle = MotorcycleResponseDTO(
            id = 1, brand = "Honda", model = "CB 500X", color = "Preta", year = 2024,
            engineCapacity = 500, owner = MotorcycleOwnerDTO(1, "Rider", "rider@example.com")
        )
    )

    @Test
    fun captionFollowsTheStatus() {
        assertTrue(tripShareText(trip("COMPLETED")).contains("Fiz esta viagem: São Paulo → Santos"))
        assertTrue(tripShareText(trip("IN_PROGRESS")).contains("Estou nesta viagem"))
        assertTrue(tripShareText(trip("PLANNED")).contains("Vou fazer esta viagem"))
    }

    @Test
    fun captionCarriesTheTripFactsAndTheBrand() {
        val text = tripShareText(trip("COMPLETED"))

        assertTrue(text.contains("70 km · Asfalto"))
        assertTrue(text.contains("Honda CB 500X"))
        assertTrue(text.endsWith("Registrado no MotoTrack"))
    }

    @Test
    fun captionCarriesTheCostsWhenTheyAreShown() {
        val costs = TripShareCosts(
            totalCost = 183.71,
            costPerKm = 2.62,
            kmPerLiter = 24.1,
            duration = null
        )

        val text = tripShareText(trip("COMPLETED"), costs)

        assertTrue(text.contains("R$ 183,71 gastos · R$ 2,62/km · 24,1 km/L"))
        assertTrue(!tripShareText(trip("COMPLETED")).contains("gastos"))
    }

    @Test
    fun cardLabelFollowsTheStatus() {
        assertEquals("VIAGEM CONCLUÍDA", tripShareStatusLabel("COMPLETED"))
        assertEquals("EM VIAGEM", tripShareStatusLabel("IN_PROGRESS"))
        assertEquals("PRÓXIMA VIAGEM", tripShareStatusLabel("PLANNED"))
    }
}
