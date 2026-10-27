package br.dev.guisleri.mototrack.data.maintenance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MaintenanceLogicTest {

    private val today = LocalDate.parse("2026-10-22")

    private fun maintenance(
        odometer: OdometerReading? = OdometerReading(km = 12_000, tripKmAtReading = 100.0, date = "2026-10-01"),
        vararg services: Pair<MaintenanceItem, ServiceRecord>,
        intervals: Map<String, ItemInterval> = emptyMap()
    ) = MotorcycleMaintenance(
        odometer = odometer,
        services = services.associate { (item, record) -> item.key to record },
        intervals = intervals
    )

    @Test
    fun odometerIsWhatTheRiderReadPlusTheTripsSince() {
        val reading = OdometerReading(km = 12_000, tripKmAtReading = 100.0, date = "2026-10-01")

        assertEquals(12_230, currentOdometerKm(reading, tripKmNow = 330.0))
        assertEquals(12_000, currentOdometerKm(reading, tripKmNow = 100.0))
    }

    @Test
    fun odometerNeverGoesBackWhenTripsAreDeleted() {
        val reading = OdometerReading(km = 12_000, tripKmAtReading = 100.0, date = "2026-10-01")

        assertEquals(12_000, currentOdometerKm(reading, tripKmNow = 40.0))
        assertEquals(12_000, currentOdometerKm(reading, tripKmNow = null))
    }

    @Test
    fun withoutAReadingThereIsNoOdometer() {
        assertNull(currentOdometerKm(null, tripKmNow = 500.0))
    }

    @Test
    fun anItemNeverRegisteredIsUnknown() {
        val progress = progressOf(MaintenanceItem.OIL, maintenance(), 12_000, today)

        assertEquals(MaintenanceLevel.UNKNOWN, progress.level)
        assertNull(progress.kmLeft)
        assertNull(progress.lastService)
    }

    @Test
    fun kmLeftCountsFromTheKmOfTheLastService() {
        val m = maintenance(
            services = arrayOf(MaintenanceItem.OIL to ServiceRecord(doneAtKm = 10_000, date = "2026-09-20"))
        )

        val progress = progressOf(MaintenanceItem.OIL, m, 12_000, today)

        // 3.000 km interval, 2.000 done.
        assertEquals(1_000, progress.kmLeft)
        assertEquals(MaintenanceLevel.OK, progress.level)
        assertEquals(0.67f, progress.usedFraction, 0.01f)
    }

    @Test
    fun anItemIsSoonWithinTenPercentOfItsKmInterval() {
        val m = maintenance(
            services = arrayOf(MaintenanceItem.OIL to ServiceRecord(doneAtKm = 9_100, date = "2026-09-20"))
        )

        val progress = progressOf(MaintenanceItem.OIL, m, 12_000, today)

        assertEquals(100, progress.kmLeft)
        assertEquals(MaintenanceLevel.SOON, progress.level)
    }

    @Test
    fun anItemIsOverdueOnceItsKmIntervalIsUsed() {
        val m = maintenance(
            services = arrayOf(MaintenanceItem.CHAIN to ServiceRecord(doneAtKm = 11_400, date = "2026-10-10"))
        )

        val progress = progressOf(MaintenanceItem.CHAIN, m, 12_000, today)

        assertEquals(-100, progress.kmLeft)
        assertEquals(MaintenanceLevel.OVERDUE, progress.level)
        assertEquals(1f, progress.usedFraction, 0f)
    }

    @Test
    fun timeAloneCanMakeAnItemDue() {
        val m = maintenance(
            services = arrayOf(MaintenanceItem.OIL to ServiceRecord(doneAtKm = 11_900, date = "2026-04-01"))
        )

        val progress = progressOf(MaintenanceItem.OIL, m, 12_000, today)

        // 6 months from 1 April is 1 October: three weeks ago.
        assertEquals(-21L, progress.daysLeft)
        assertEquals(MaintenanceLevel.OVERDUE, progress.level)
    }

    @Test
    fun anItemIsSoonWithinThirtyDaysOfItsDate() {
        val m = maintenance(
            services = arrayOf(MaintenanceItem.OIL to ServiceRecord(doneAtKm = 11_900, date = "2026-04-30"))
        )

        val progress = progressOf(MaintenanceItem.OIL, m, 12_000, today)

        assertEquals(8L, progress.daysLeft)
        assertEquals(MaintenanceLevel.SOON, progress.level)
    }

    @Test
    fun withoutTheOdometerOnlyTheDateCounts() {
        val m = maintenance(
            odometer = null,
            services = arrayOf(MaintenanceItem.OIL to ServiceRecord(doneAtKm = 10_000, date = "2026-09-20"))
        )

        val progress = progressOf(MaintenanceItem.OIL, m, odometerKm = null, today = today)

        assertNull(progress.kmLeft)
        assertEquals(MaintenanceLevel.OK, progress.level)
    }

    @Test
    fun aServiceWithoutKmOnlyCountsByDate() {
        val m = maintenance(
            services = arrayOf(MaintenanceItem.CHAIN to ServiceRecord(doneAtKm = null, date = "2026-10-01"))
        )

        val progress = progressOf(MaintenanceItem.CHAIN, m, 12_000, today)

        // The chain has no time interval, so there is nothing to count.
        assertNull(progress.kmLeft)
        assertNull(progress.daysLeft)
        assertEquals(MaintenanceLevel.OK, progress.level)
    }

    @Test
    fun aCustomIntervalReplacesTheDefault() {
        val m = maintenance(
            services = arrayOf(MaintenanceItem.OIL to ServiceRecord(doneAtKm = 10_000, date = "2026-09-20")),
            intervals = mapOf(MaintenanceItem.OIL.key to ItemInterval(km = 2_000, months = null))
        )

        val progress = progressOf(MaintenanceItem.OIL, m, 12_000, today)

        assertEquals(0, progress.kmLeft)
        assertEquals(MaintenanceLevel.OVERDUE, progress.level)
    }

    @Test
    fun overviewPutsTheMostUrgentFirstAndTheUnknownLast() {
        val m = maintenance(
            services = arrayOf(
                MaintenanceItem.OIL to ServiceRecord(doneAtKm = 11_000, date = "2026-09-20"),
                MaintenanceItem.CHAIN to ServiceRecord(doneAtKm = 11_000, date = "2026-09-20"),
                MaintenanceItem.AIR_FILTER to ServiceRecord(doneAtKm = 11_900, date = "2026-09-20")
            )
        )

        val overview = maintenanceOverview(m, tripKmNow = 100.0, today = today)

        assertEquals(MaintenanceItem.CHAIN, overview.first().item)
        assertEquals(MaintenanceLevel.OVERDUE, overview.first().level)
        assertTrue(overview.takeLast(3).all { it.level == MaintenanceLevel.UNKNOWN })
        assertEquals(MaintenanceItem.entries.size, overview.size)
    }

    @Test
    fun summaryCountsEachLevel() {
        val m = maintenance(
            services = arrayOf(
                MaintenanceItem.CHAIN to ServiceRecord(doneAtKm = 11_000, date = "2026-09-20"),
                MaintenanceItem.OIL to ServiceRecord(doneAtKm = 9_100, date = "2026-09-20")
            )
        )

        val summary = summarize(maintenanceOverview(m, tripKmNow = 100.0, today = today))

        assertEquals(1, summary.overdue)
        assertEquals(1, summary.soon)
        assertEquals(4, summary.unknown)
        assertTrue(summary.needsAttention)
    }
}
