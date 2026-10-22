package br.dev.guisleri.mototrack.data.maintenance

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToInt

/** How close an item is to being due. */
enum class MaintenanceLevel {
    /** Never registered: there is nothing to count from. */
    UNKNOWN,
    OK,
    SOON,
    OVERDUE
}

data class MaintenanceProgress(
    val item: MaintenanceItem,
    val level: MaintenanceLevel,
    val interval: ItemInterval,
    val lastService: ServiceRecord?,
    // Negative when overdue; null when that measure does not apply or cannot be known.
    val kmLeft: Int?,
    val daysLeft: Long?,
    // How much of the interval is used, 0..1 (1 when overdue).
    val usedFraction: Float
)

data class MaintenanceSummary(
    val overdue: Int,
    val soon: Int,
    val unknown: Int
) {
    val needsAttention: Boolean get() = overdue > 0 || soon > 0
}

private const val SOON_FRACTION = 0.1
private const val SOON_MIN_KM = 50
private const val SOON_DAYS = 30L

/**
 * The motorcycle's km now: what the rider read on the panel plus the trips done
 * since. Null until the rider has told the odometer once.
 */
fun currentOdometerKm(
    odometer: OdometerReading?,
    tripKmNow: Double?
): Int? {
    odometer ?: return null

    val sinceReading = max(0.0, (tripKmNow ?: odometer.tripKmAtReading) - odometer.tripKmAtReading)

    return odometer.km + sinceReading.roundToInt()
}

fun intervalOf(item: MaintenanceItem, maintenance: MotorcycleMaintenance): ItemInterval =
    maintenance.intervals[item.key] ?: ItemInterval(item.defaultKm, item.defaultMonths)

fun progressOf(
    item: MaintenanceItem,
    maintenance: MotorcycleMaintenance,
    odometerKm: Int?,
    today: LocalDate
): MaintenanceProgress {
    val interval = intervalOf(item, maintenance)
    val service = maintenance.services[item.key]

    if (service == null) {
        return MaintenanceProgress(
            item = item,
            level = MaintenanceLevel.UNKNOWN,
            interval = interval,
            lastService = null,
            kmLeft = null,
            daysLeft = null,
            usedFraction = 0f
        )
    }

    val kmLeft =
        if (interval.km != null && service.doneAtKm != null && odometerKm != null) {
            interval.km - (odometerKm - service.doneAtKm)
        } else {
            null
        }

    val dueDate = interval.months?.let { months ->
        runCatching { LocalDate.parse(service.date).plusMonths(months.toLong()) }.getOrNull()
    }
    val daysLeft = dueDate?.let { ChronoUnit.DAYS.between(today, it) }

    val kmFraction = kmLeft?.let { 1.0 - it.toDouble() / interval.km!! }
    val timeFraction = daysLeft?.let { left ->
        val totalDays = ChronoUnit.DAYS.between(LocalDate.parse(service.date), dueDate)
        if (totalDays > 0) 1.0 - left.toDouble() / totalDays else 1.0
    }
    val used = listOfNotNull(kmFraction, timeFraction).maxOrNull() ?: 0.0

    val overdue = (kmLeft != null && kmLeft <= 0) || (daysLeft != null && daysLeft <= 0)
    val soon = (kmLeft != null && interval.km != null &&
            kmLeft <= max(SOON_MIN_KM.toDouble(), interval.km * SOON_FRACTION)) ||
            (daysLeft != null && daysLeft <= SOON_DAYS)

    return MaintenanceProgress(
        item = item,
        level = when {
            overdue -> MaintenanceLevel.OVERDUE
            soon -> MaintenanceLevel.SOON
            else -> MaintenanceLevel.OK
        },
        interval = interval,
        lastService = service,
        kmLeft = kmLeft,
        daysLeft = daysLeft,
        usedFraction = used.toFloat().coerceIn(0f, 1f)
    )
}

/** Every item, the most urgent first and the never-registered ones last. */
fun maintenanceOverview(
    maintenance: MotorcycleMaintenance,
    tripKmNow: Double?,
    today: LocalDate
): List<MaintenanceProgress> {
    val odometerKm = currentOdometerKm(maintenance.odometer, tripKmNow)

    return MaintenanceItem.entries
        .map { progressOf(it, maintenance, odometerKm, today) }
        .sortedWith(
            compareBy<MaintenanceProgress> { urgencyRank(it.level) }
                .thenByDescending { it.usedFraction }
        )
}

fun summarize(overview: List<MaintenanceProgress>): MaintenanceSummary =
    MaintenanceSummary(
        overdue = overview.count { it.level == MaintenanceLevel.OVERDUE },
        soon = overview.count { it.level == MaintenanceLevel.SOON },
        unknown = overview.count { it.level == MaintenanceLevel.UNKNOWN }
    )

private fun urgencyRank(level: MaintenanceLevel): Int =
    when (level) {
        MaintenanceLevel.OVERDUE -> 0
        MaintenanceLevel.SOON -> 1
        MaintenanceLevel.OK -> 2
        MaintenanceLevel.UNKNOWN -> 3
    }
