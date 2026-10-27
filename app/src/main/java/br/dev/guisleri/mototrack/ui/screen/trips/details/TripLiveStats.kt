package br.dev.guisleri.mototrack.ui.screen.trips.details

import br.dev.guisleri.mototrack.data.model.trip.TripCostSummaryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import java.time.Duration
import java.time.Instant

private const val MIN_MINUTES_FOR_SPEED = 10

/** How long the trip has been going, or took: null when the start was never recorded. */
internal fun tripElapsed(trip: TripResponseDTO, now: Instant): Duration? {
    val started = parseInstant(trip.startedAt) ?: return null
    val end = when (trip.status) {
        "COMPLETED" -> parseInstant(trip.completedAt) ?: return null
        else -> now
    }

    return Duration.between(started, end).takeIf { !it.isNegative } ?: Duration.ZERO
}

internal fun parseInstant(value: String?): Instant? =
    value?.let { runCatching { Instant.parse(it) }.getOrNull() }

/** Numbers derived from the trip and its summary; each is null while it means nothing. */
internal data class TripLiveStats(
    val totalCost: Double?,
    val costPerKm: Double?,
    val liters: Double?,
    val fuelCost: Double?,
    val averagePricePerLiter: Double?,
    val tollCount: Long,
    val tollCost: Double,
    val otherCost: Double,
    // Distance over liters: only an honest figure once the whole trip is done.
    val kmPerLiter: Double?,
    val averageSpeedKmh: Double?
)

internal fun tripLiveStats(
    trip: TripResponseDTO,
    summary: TripCostSummaryResponseDTO?,
    elapsed: Duration?
): TripLiveStats {
    val liters = summary?.fuel?.liters?.takeIf { it > 0.0 }
    val fuelCost = summary?.fuel?.totalCost
    val toll = summary?.expenses?.firstOrNull { it.category == "TOLL" }
    val tollCost = toll?.total ?: 0.0
    val others = summary?.expenses.orEmpty()
        .filter { it.category != "TOLL" }
        .sumOf { it.total }
    val completed = trip.status == "COMPLETED"

    return TripLiveStats(
        totalCost = summary?.totalCost,
        costPerKm = summary?.costPerKm,
        liters = liters,
        fuelCost = fuelCost,
        averagePricePerLiter =
            if (liters != null && fuelCost != null) fuelCost / liters else null,
        tollCount = toll?.count ?: 0,
        tollCost = tollCost,
        otherCost = others,
        kmPerLiter =
            if (completed && liters != null && trip.distanceKm > 0) trip.distanceKm / liters else null,
        averageSpeedKmh =
            // A few minutes of trip (a test, a forgotten start) would give a silly speed.
            if (completed && elapsed != null && elapsed.toMinutes() >= MIN_MINUTES_FOR_SPEED) {
                trip.distanceKm / (elapsed.toMinutes() / 60.0)
            } else {
                null
            }
    )
}

/** "02:41:09", or "1d 03:12:09" past a day: a clock that visibly runs. */
internal fun formatClock(duration: Duration): String {
    val totalSeconds = duration.seconds.coerceAtLeast(0)
    val days = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60

    val clock = "%02d:%02d:%02d".format(hours, minutes, seconds)

    return if (days > 0) "${days}d $clock" else clock
}

/** "2 h 41 min", "35 min", "1 d 3 h": a length for a finished trip. */
internal fun formatDuration(duration: Duration): String {
    val totalMinutes = duration.toMinutes().coerceAtLeast(0)
    val days = totalMinutes / 1_440
    val hours = (totalMinutes % 1_440) / 60
    val minutes = totalMinutes % 60

    return when {
        days > 0 -> if (hours > 0) "$days d $hours h" else "$days d"
        hours > 0 -> if (minutes > 0) "$hours h $minutes min" else "$hours h"
        else -> "$minutes min"
    }
}
