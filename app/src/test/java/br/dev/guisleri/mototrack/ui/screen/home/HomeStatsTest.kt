package br.dev.guisleri.mototrack.ui.screen.home

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeStatsTest {

    private val motorcycle = MotorcycleResponseDTO(
        id = 1, brand = "Honda", model = "CB 500X", color = "Preta", year = 2024,
        engineCapacity = 500,
        owner = MotorcycleOwnerDTO(1, "Rider", "rider@example.com")
    )

    private fun trip(date: String, km: Double, status: String = "COMPLETED") =
        TripResponseDTO(
            id = 1, origin = "A", destination = "B", distanceKm = km, status = status,
            terrain = "ASPHALT", tripDate = date, motorcycle = motorcycle
        )

    @Test
    fun sumsCompletedDistanceIntoTheRightMonth() {
        val months = monthlyCompletedDistanceKm(
            listOf(
                trip("2026-01-10", 100.0),
                trip("2026-01-25", 50.5),
                trip("2026-10-05", 190.0)
            ),
            2026
        )

        assertEquals(150.5, months[0], 0.0)
        assertEquals(190.0, months[9], 0.0)
        assertEquals(0.0, months[5], 0.0)
    }

    @Test
    fun ignoresOtherYearsAndUnfinishedTrips() {
        val months = monthlyCompletedDistanceKm(
            listOf(
                trip("2025-10-05", 300.0),
                trip("2026-10-05", 200.0, status = "PLANNED"),
                trip("2026-10-06", 100.0, status = "IN_PROGRESS")
            ),
            2026
        )

        assertEquals(0.0, months.sum(), 0.0)
    }

    @Test
    fun ignoresTripsWithUnreadableDates() {
        val months = monthlyCompletedDistanceKm(listOf(trip("not-a-date", 100.0)), 2026)

        assertEquals(0.0, months.sum(), 0.0)
    }

    @Test
    fun firstMilestoneIsOneHundredKilometres() {
        val milestone = milestoneFor(0.0)

        assertEquals(0, milestone.previousKm)
        assertEquals(100, milestone.targetKm)
        assertEquals(100.0, milestone.remainingKm, 0.0)
        assertEquals(0f, milestone.fraction, 0f)
    }

    @Test
    fun midwayBetweenMilestones() {
        val milestone = milestoneFor(675.0)

        assertEquals(500, milestone.previousKm)
        assertEquals(1_000, milestone.targetKm)
        assertEquals(325.0, milestone.remainingKm, 0.0)
        assertEquals(0.35f, milestone.fraction, 0.0001f)
    }

    @Test
    fun reachingAMilestoneMovesToTheNextOne() {
        val milestone = milestoneFor(1_000.0)

        assertEquals(1_000, milestone.previousKm)
        assertEquals(2_500, milestone.targetKm)
        assertEquals(0f, milestone.fraction, 0f)
    }

    @Test
    fun partialKilometresRoundTheRemainingUp() {
        assertEquals(26.0, milestoneFor(974.2).remainingKm, 0.0)
    }

    @Test
    fun beyondTheLastMilestoneStepsByFiftyThousand() {
        val milestone = milestoneFor(120_000.0)

        assertEquals(100_000, milestone.previousKm)
        assertEquals(150_000, milestone.targetKm)
        assertEquals(30_000.0, milestone.remainingKm, 0.0)
    }

    @Test
    fun negativeTotalIsTreatedAsZero() {
        assertEquals(100, milestoneFor(-5.0).targetKm)
    }

    @Test
    fun firstNameKeepsOnlyTheFirstWord() {
        assertEquals("Marcos", firstNameOf("Marcos Guisleri"))
    }

    @Test
    fun firstNameIsNormalisedWhateverTheTypedCase() {
        assertEquals("Marcos", firstNameOf("MARCOS silva"))
        assertEquals("Ana", firstNameOf("ana"))
    }

    @Test
    fun firstNameIgnoresSurroundingAndRepeatedSpaces() {
        assertEquals("Marcos", firstNameOf("   marcos    guisleri  "))
    }

    @Test
    fun firstNameKeepsCompoundNamesTogether() {
        assertEquals("Maria-Clara", firstNameOf("maria-clara souza"))
    }

    @Test
    fun noUsableNameGivesNoFirstName() {
        assertEquals(null, firstNameOf(null))
        assertEquals(null, firstNameOf(""))
        assertEquals(null, firstNameOf("   "))
    }
}
