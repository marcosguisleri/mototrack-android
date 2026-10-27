package br.dev.guisleri.mototrack.data.maintenance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MaintenanceAlertsTest {

    private val today = LocalDate.parse("2026-10-27")

    // Odometer 12.000 with 0 km of trips; the oil was changed at the given km.
    private fun withOil(doneAtKm: Int, alerted: Map<String, String> = emptyMap()) = MotorcycleMaintenance(
        odometer = OdometerReading(12_000, 0.0, "2026-10-01"),
        services = mapOf(MaintenanceItem.OIL.key to ServiceRecord(doneAtKm, "2026-10-01")),
        tripKmSnapshot = 0.0,
        alerted = alerted
    )

    @Test
    fun anItemComingUpIsToldAboutOnce() {
        val maintenance = withOil(doneAtKm = 9_100) // 100 km left of 3.000

        val first = planMaintenanceAlerts(maintenance, today)
        assertEquals(listOf(MaintenanceLevel.SOON), first.alerts.map { it.level })
        assertEquals(mapOf("oil" to "SOON"), first.alerted)

        val second = planMaintenanceAlerts(maintenance.copy(alerted = first.alerted), today)
        assertTrue(second.alerts.isEmpty())
    }

    @Test
    fun anItemBecomingOverdueIsToldAboutAgain() {
        val soonAlready = withOil(doneAtKm = 8_900, alerted = mapOf("oil" to "SOON"))

        val plan = planMaintenanceAlerts(soonAlready, today) // -100 km: overdue

        assertEquals(listOf(MaintenanceLevel.OVERDUE), plan.alerts.map { it.level })
        assertEquals(mapOf("oil" to "OVERDUE"), plan.alerted)
    }

    @Test
    fun anOverdueItemIsNotRepeatedEveryDay() {
        val told = withOil(doneAtKm = 8_900, alerted = mapOf("oil" to "OVERDUE"))

        assertTrue(planMaintenanceAlerts(told, today).alerts.isEmpty())
    }

    @Test
    fun anItemDoneAgainIsForgottenSoTheNextTimeCountsAsNew() {
        val doneAgain = withOil(doneAtKm = 12_000, alerted = mapOf("oil" to "OVERDUE"))

        val plan = planMaintenanceAlerts(doneAgain, today)

        assertTrue(plan.alerts.isEmpty())
        assertTrue(plan.alerted.isEmpty())
    }

    @Test
    fun nothingRegisteredNeverAlerts() {
        val plan = planMaintenanceAlerts(MotorcycleMaintenance(), today)

        assertTrue(plan.alerts.isEmpty())
    }

    @Test
    fun timeAloneIsEnoughToAlert() {
        val byDate = MotorcycleMaintenance(
            services = mapOf(MaintenanceItem.OIL.key to ServiceRecord(null, "2026-04-01"))
        )

        val plan = planMaintenanceAlerts(byDate, today)

        // 6 months from 1 April is 1 October: overdue.
        assertEquals(listOf(MaintenanceLevel.OVERDUE), plan.alerts.map { it.level })
    }

    @Test
    fun theMostUrgentComeFirst() {
        val maintenance = MotorcycleMaintenance(
            odometer = OdometerReading(12_000, 0.0, "2026-10-01"),
            services = mapOf(
                MaintenanceItem.OIL.key to ServiceRecord(9_100, "2026-10-01"),
                MaintenanceItem.CHAIN.key to ServiceRecord(11_000, "2026-10-01")
            ),
            tripKmSnapshot = 0.0
        )

        val plan = planMaintenanceAlerts(maintenance, today)

        assertEquals(listOf(MaintenanceItem.CHAIN, MaintenanceItem.OIL), plan.alerts.map { it.item })
    }
}
