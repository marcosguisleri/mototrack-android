package br.dev.guisleri.mototrack.ui.screen.trips.details

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.ExpenseCategoryCostDTO
import br.dev.guisleri.mototrack.data.model.trip.FuelCostDTO
import br.dev.guisleri.mototrack.data.model.trip.TripCostSummaryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.Instant

class TripLiveStatsTest {

    private val started = "2026-10-20T10:00:00Z"
    private val now = Instant.parse("2026-10-20T12:41:09Z")

    @Test
    fun elapsedOfATripOnTheRoadRunsUntilNow() {
        val elapsed = tripElapsed(trip("IN_PROGRESS", startedAt = started), now)

        assertEquals(Duration.ofHours(2).plusMinutes(41).plusSeconds(9), elapsed)
    }

    @Test
    fun elapsedOfACompletedTripIsItsRecordedLength() {
        val elapsed = tripElapsed(
            trip("COMPLETED", startedAt = started, completedAt = "2026-10-20T14:30:00Z"),
            now
        )

        assertEquals(Duration.ofHours(4).plusMinutes(30), elapsed)
    }

    @Test
    fun elapsedIsUnknownWithoutAStartOrAnEnd() {
        assertNull(tripElapsed(trip("IN_PROGRESS"), now))
        assertNull(tripElapsed(trip("COMPLETED", startedAt = started), now))
    }

    @Test
    fun aClockSkewNeverMakesTheClockNegative() {
        val elapsed = tripElapsed(trip("IN_PROGRESS", startedAt = "2026-10-20T13:00:00Z"), now)

        assertEquals(Duration.ZERO, elapsed)
    }

    @Test
    fun clockFormatsHoursAndDays() {
        assertEquals("02:41:09", formatClock(Duration.ofSeconds(2 * 3600 + 41 * 60 + 9)))
        assertEquals("00:00:00", formatClock(Duration.ZERO))
        assertEquals("1d 03:12:09", formatClock(Duration.ofSeconds(86_400 + 3 * 3600 + 12 * 60 + 9)))
    }

    @Test
    fun durationFormatsTheLengthOfAFinishedTrip() {
        assertEquals("35 min", formatDuration(Duration.ofMinutes(35)))
        assertEquals("2 h", formatDuration(Duration.ofHours(2)))
        assertEquals("2 h 41 min", formatDuration(Duration.ofMinutes(161)))
        assertEquals("1 d 3 h", formatDuration(Duration.ofHours(27)))
    }

    @Test
    fun liveStatsTurnTheSummaryIntoTheNumbersOfThePanel() {
        val stats = tripLiveStats(trip("IN_PROGRESS"), summary(), elapsed = null)

        assertEquals(183.71, stats.totalCost!!, 0.001)
        assertEquals(1.08, stats.costPerKm!!, 0.001)
        assertEquals(16.2, stats.liters!!, 0.001)
        assertEquals(103.9 / 16.2, stats.averagePricePerLiter!!, 0.001)
        assertEquals(3L, stats.tollCount)
        assertEquals(37.8, stats.tollCost, 0.001)
        assertEquals(42.01, stats.otherCost, 0.001)
    }

    @Test
    fun consumptionAndSpeedOnlyExistOnceTheTripIsCompleted() {
        val duration = Duration.ofHours(2)

        val live = tripLiveStats(trip("IN_PROGRESS"), summary(), duration)
        assertNull(live.kmPerLiter)
        assertNull(live.averageSpeedKmh)

        val done = tripLiveStats(trip("COMPLETED", distanceKm = 170.0), summary(), duration)
        assertEquals(170.0 / 16.2, done.kmPerLiter!!, 0.001)
        assertEquals(85.0, done.averageSpeedKmh!!, 0.001)
    }

    @Test
    fun aTripOfAFewMinutesHasNoAverageSpeed() {
        val stats = tripLiveStats(trip("COMPLETED"), summary(), Duration.ofMinutes(3))

        assertNull(stats.averageSpeedKmh)
    }

    @Test
    fun withNothingRecordedThereAreNoFigures() {
        val empty = TripCostSummaryResponseDTO(FuelCostDTO(0), emptyList())

        val stats = tripLiveStats(trip("IN_PROGRESS"), empty, elapsed = null)

        assertNull(stats.totalCost)
        assertNull(stats.liters)
        assertNull(stats.averagePricePerLiter)
        assertEquals(0L, stats.tollCount)
        assertNotNull(stats.otherCost)
    }

    private fun summary() = TripCostSummaryResponseDTO(
        fuel = FuelCostDTO(entries = 2, liters = 16.2, totalCost = 103.9),
        expenses = listOf(
            ExpenseCategoryCostDTO("TOLL", 3, 37.8),
            ExpenseCategoryCostDTO("FOOD", 1, 42.01)
        ),
        totalCost = 183.71,
        costPerKm = 1.08
    )

    private fun trip(
        status: String,
        startedAt: String? = null,
        completedAt: String? = null,
        distanceKm: Double = 170.0
    ) = TripResponseDTO(
        id = 1,
        origin = "São Paulo",
        destination = "Santos",
        distanceKm = distanceKm,
        status = status,
        terrain = "ASPHALT",
        tripDate = "2026-10-20",
        motorcycle = MotorcycleResponseDTO(
            id = 1,
            brand = "Honda",
            model = "CB 500X",
            color = "Preta",
            year = 2024,
            engineCapacity = 500,
            owner = MotorcycleOwnerDTO(1, "Rider", "rider@example.com")
        ),
        startedAt = startedAt,
        completedAt = completedAt
    )
}
