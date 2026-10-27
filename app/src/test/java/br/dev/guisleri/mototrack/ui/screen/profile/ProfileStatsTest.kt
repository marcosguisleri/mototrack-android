package br.dev.guisleri.mototrack.ui.screen.profile

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileStatsTest {

    private val motorcycle = MotorcycleResponseDTO(
        id = 1, brand = "Honda", model = "CB 500X", color = "Preta", year = 2024,
        engineCapacity = 500,
        owner = MotorcycleOwnerDTO(1, "Rider", "rider@example.com")
    )

    private var nextId = 1L

    private fun trip(
        destination: String,
        km: Double = 100.0,
        date: String = "2026-05-01",
        status: String = "COMPLETED",
        terrain: String = "ASPHALT"
    ) = TripResponseDTO(
        id = nextId++, origin = "São Paulo", destination = destination, distanceKm = km,
        status = status, terrain = terrain, tripDate = date, motorcycle = motorcycle
    )

    private fun achievement(stats: ProfileStats, kind: AchievementKind) =
        stats.achievements.first { it.kind == kind }

    @Test
    fun noTripsMeansNothingUnlocked() {
        val stats = profileStatsOf(emptyList(), motorcycleCount = 0)

        assertEquals(0, stats.completedTrips)
        assertEquals(0, stats.unlockedCount)
        assertTrue(stats.topDestinations.isEmpty())
    }

    @Test
    fun onlyCompletedTripsCount() {
        val stats = profileStatsOf(
            listOf(
                trip("Santos", 70.0),
                trip("Ubatuba", 200.0, status = "PLANNED"),
                trip("Paraty", 300.0, status = "IN_PROGRESS")
            ),
            motorcycleCount = 1
        )

        assertEquals(1, stats.completedTrips)
        assertEquals(70.0, stats.totalKm, 0.0)
        assertEquals(1, stats.destinationCount)
        assertTrue(achievement(stats, AchievementKind.FIRST_TRIP).unlocked)
        assertFalse(achievement(stats, AchievementKind.KM_100).unlocked)
    }

    @Test
    fun distanceAchievementsUnlockAtTheirThresholdAndReportProgress() {
        val stats = profileStatsOf(listOf(trip("Santos", 100.0), trip("Bertioga", 400.0)), 1)

        assertTrue(achievement(stats, AchievementKind.KM_100).unlocked)
        val thousand = achievement(stats, AchievementKind.KM_1000)
        assertFalse(thousand.unlocked)
        assertEquals(0.5f, thousand.fraction, 0.0001f)
    }

    @Test
    fun longHaulUsesTheLongestSingleTrip() {
        val short = profileStatsOf(listOf(trip("A", 200.0), trip("B", 200.0)), 1)
        val long = profileStatsOf(listOf(trip("A", 300.0)), 1)

        assertFalse(achievement(short, AchievementKind.LONG_HAUL).unlocked)
        assertTrue(achievement(long, AchievementKind.LONG_HAUL).unlocked)
    }

    @Test
    fun offRoadNeedsACompletedOffRoadTrip() {
        val planned = profileStatsOf(listOf(trip("A", terrain = "OFF_ROAD", status = "PLANNED")), 1)
        val done = profileStatsOf(listOf(trip("A", terrain = "OFF_ROAD")), 1)

        assertFalse(achievement(planned, AchievementKind.OFF_ROAD).unlocked)
        assertTrue(achievement(done, AchievementKind.OFF_ROAD).unlocked)
    }

    @Test
    fun explorerAndFrequentTravellerCountDistinctDestinationsAndTrips() {
        val sameDestination = List(10) { trip("Santos") }
        val stats = profileStatsOf(sameDestination, 1)

        assertTrue(achievement(stats, AchievementKind.TEN_TRIPS).unlocked)
        assertFalse(achievement(stats, AchievementKind.EXPLORER).unlocked)

        val five = profileStatsOf(listOf("A", "B", "C", "D", "E").map { trip(it) }, 1)
        assertTrue(achievement(five, AchievementKind.EXPLORER).unlocked)
    }

    @Test
    fun garageAchievementFollowsTheMotorcycleCount() {
        assertFalse(
            achievement(profileStatsOf(emptyList(), 2), AchievementKind.GARAGE).unlocked
        )
        assertTrue(
            achievement(profileStatsOf(emptyList(), 3), AchievementKind.GARAGE).unlocked
        )
    }

    @Test
    fun destinationsAreGroupedIgnoringCaseAndSpacesAndSortedByVisits() {
        val visits = visitsByDestination(
            listOf(
                trip("Santos", 70.0, "2026-01-10"),
                trip("  santos ", 70.0, "2026-03-10"),
                trip("Ubatuba", 230.0, "2026-04-01"),
                trip("SANTOS", 70.0, "2026-05-20"),
                trip("Paraty", 300.0, "2026-06-01")
            )
        )

        assertEquals(listOf("Paraty", "Ubatuba"), visits.drop(1).map { it.name })
        assertEquals(3, visits.first().visits)
        assertEquals(210.0, visits.first().totalKm, 0.0)
        assertEquals("SANTOS", visits.first().name)
    }

    @Test
    fun tiesGoToTheMostRecentlyVisitedDestination() {
        val visits = visitsByDestination(
            listOf(
                trip("Antiga", date = "2026-01-01"),
                trip("Recente", date = "2026-08-01")
            )
        )

        assertEquals(listOf("Recente", "Antiga"), visits.map { it.name })
    }

    @Test
    fun topDestinationsAreLimited() {
        val stats = profileStatsOf(listOf("A", "B", "C", "D", "E").map { trip(it) }, 1)

        assertEquals(TOP_DESTINATIONS_LIMIT, stats.topDestinations.size)
        assertEquals(5, stats.destinationCount)
    }

    @Test
    fun blankDestinationsAreIgnored() {
        assertTrue(visitsByDestination(listOf(trip("  "))).isEmpty())
    }
}
