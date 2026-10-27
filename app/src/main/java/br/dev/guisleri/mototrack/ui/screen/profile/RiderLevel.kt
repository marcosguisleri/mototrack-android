package br.dev.guisleri.mototrack.ui.screen.profile

import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import kotlin.math.roundToInt

private const val XP_PER_COMPLETED_TRIP = 50
private const val XP_PER_DESTINATION = 25

/** The levels of a rider: the name and the XP it starts at. */
private val LEVELS = listOf(
    "Novato" to 0,
    "Estradeiro" to 100,
    "Viajante" to 300,
    "Desbravador" to 800,
    "Veterano" to 2_000,
    "Mestre da Estrada" to 5_000,
    "Lenda" to 12_000
)

internal data class RiderLevel(
    val level: Int,
    val name: String,
    val xp: Int,
    val startXp: Int,
    // Null at the top level.
    val nextName: String?,
    val nextXp: Int?
) {
    val isMax: Boolean get() = nextXp == null

    val xpToNext: Int get() = ((nextXp ?: xp) - xp).coerceAtLeast(0)

    /** How far between this level and the next, 0..1 (1 at the top). */
    val fraction: Float
        get() = nextXp
            ?.let { ((xp - startXp).toFloat() / (it - startXp)).coerceIn(0f, 1f) }
            ?: 1f
}

/** 1 XP per km of completed trips, 50 per completed trip and 25 per destination visited. */
internal fun riderXp(completedTrips: Int, totalKm: Double, destinationCount: Int): Int =
    (totalKm + completedTrips * XP_PER_COMPLETED_TRIP + destinationCount * XP_PER_DESTINATION).roundToInt()

internal fun riderLevelOfXp(xp: Int): RiderLevel {
    val safe = xp.coerceAtLeast(0)
    val index = LEVELS.indexOfLast { safe >= it.second }.coerceAtLeast(0)
    val next = LEVELS.getOrNull(index + 1)

    return RiderLevel(
        level = index + 1,
        name = LEVELS[index].first,
        xp = safe,
        startXp = LEVELS[index].second,
        nextName = next?.first,
        nextXp = next?.second
    )
}

internal fun riderLevelOf(stats: ProfileStats): RiderLevel =
    riderLevelOfXp(riderXp(stats.completedTrips, stats.totalKm, stats.destinationCount))

/** The level a rider's trips add up to (only completed trips count). */
internal fun riderLevelOf(trips: List<TripResponseDTO>): RiderLevel {
    val completed = trips.filter { it.status == "COMPLETED" }

    return riderLevelOfXp(
        riderXp(
            completedTrips = completed.size,
            totalKm = completed.sumOf { it.distanceKm },
            destinationCount = visitsByDestination(completed).size
        )
    )
}
