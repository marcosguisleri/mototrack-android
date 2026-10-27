package br.dev.guisleri.mototrack.data.maintenance

import java.time.LocalDate

/** An item that has just become worth telling the rider about. */
data class MaintenanceAlert(
    val item: MaintenanceItem,
    val level: MaintenanceLevel,
    val progress: MaintenanceProgress
)

data class MaintenanceAlertPlan(
    val alerts: List<MaintenanceAlert>,
    // What to remember as already told, to save back.
    val alerted: Map<String, String>
)

private fun rank(level: String?): Int =
    when (level) {
        MaintenanceLevel.SOON.name -> 1
        MaintenanceLevel.OVERDUE.name -> 2
        else -> 0
    }

/**
 * The items to notify about now. An item is told about when it comes up (SOON) and
 * again when it becomes overdue, never twice at the same level; once it is back in
 * order (done again) it is forgotten, so the next time counts as new.
 */
fun planMaintenanceAlerts(
    maintenance: MotorcycleMaintenance,
    today: LocalDate
): MaintenanceAlertPlan {
    val overview = maintenanceOverview(maintenance, maintenance.tripKmSnapshot, today)
    val alerts = mutableListOf<MaintenanceAlert>()
    val alerted = maintenance.alerted.toMutableMap()

    overview.forEach { progress ->
        val key = progress.item.key

        when (progress.level) {
            MaintenanceLevel.SOON, MaintenanceLevel.OVERDUE -> {
                if (rank(progress.level.name) > rank(alerted[key])) {
                    alerts += MaintenanceAlert(progress.item, progress.level, progress)
                    alerted[key] = progress.level.name
                }
            }

            MaintenanceLevel.OK, MaintenanceLevel.UNKNOWN -> alerted.remove(key)
        }
    }

    return MaintenanceAlertPlan(
        alerts = alerts.sortedByDescending { rank(it.level.name) },
        alerted = alerted
    )
}
