package br.dev.guisleri.mototrack.ui.screen.home

import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.floor

private val MILESTONES_KM = listOf(
    100, 250, 500, 1_000, 2_500, 5_000, 10_000, 25_000, 50_000, 100_000
)

private const val LATE_MILESTONE_STEP_KM = 50_000

/** Kilometres of completed trips in each month (index 0 is January) of [year]. */
internal fun monthlyCompletedDistanceKm(
    trips: List<TripResponseDTO>,
    year: Int
): DoubleArray {
    val months = DoubleArray(12)

    trips
        .filter { it.status == "COMPLETED" }
        .forEach { trip ->
            val date = runCatching { LocalDate.parse(trip.tripDate) }.getOrNull()
                ?: return@forEach

            if (date.year == year) {
                months[date.monthValue - 1] += trip.distanceKm
            }
        }

    return months
}

internal data class MilestoneProgress(
    val previousKm: Int,
    val targetKm: Int,
    val remainingKm: Double,
    val fraction: Float
)

/** The next round number of kilometres to reach and how far along the way the rider is. */
internal fun milestoneFor(totalKm: Double): MilestoneProgress {
    val total = totalKm.coerceAtLeast(0.0)

    val target: Int
    val previous: Int

    if (total < MILESTONES_KM.last()) {
        target = MILESTONES_KM.first { it > total }
        previous = MILESTONES_KM.lastOrNull { it <= total } ?: 0
    } else {
        target = (floor(total / LATE_MILESTONE_STEP_KM).toInt() + 1) * LATE_MILESTONE_STEP_KM
        previous = target - LATE_MILESTONE_STEP_KM
    }

    val fraction = ((total - previous) / (target - previous)).toFloat().coerceIn(0f, 1f)

    return MilestoneProgress(
        previousKm = previous,
        targetKm = target,
        remainingKm = ceil(target - total),
        fraction = fraction
    )
}

/**
 * First name to greet the rider with, normalised ("MARCOS silva" becomes "Marcos"),
 * or null when there is no usable name.
 */
internal fun firstNameOf(fullName: String?): String? =
    fullName
        ?.trim()
        ?.split(Regex("\\s+"))
        ?.firstOrNull()
        ?.takeIf { it.isNotEmpty() }
        ?.split("-")
        ?.joinToString("-") { part ->
            part.lowercase().replaceFirstChar { it.uppercase() }
        }
