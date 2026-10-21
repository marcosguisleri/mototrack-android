package br.dev.guisleri.mototrack.ui.screen.profile

import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import java.time.LocalDate

internal enum class AchievementKind {
    FIRST_TRIP,
    TEN_TRIPS,
    KM_100,
    KM_1000,
    KM_10000,
    EXPLORER,
    LONG_HAUL,
    OFF_ROAD,
    GARAGE
}

internal data class Achievement(
    val kind: AchievementKind,
    val title: String,
    val description: String,
    val current: Double,
    val target: Double
) {
    val unlocked: Boolean get() = current >= target

    val fraction: Float get() = (current / target).toFloat().coerceIn(0f, 1f)
}

internal data class DestinationVisit(
    val name: String,
    val visits: Int,
    val totalKm: Double
)

internal data class ProfileStats(
    val completedTrips: Int,
    val totalKm: Double,
    val destinationCount: Int,
    val topDestinations: List<DestinationVisit>,
    val achievements: List<Achievement>
) {
    val unlockedCount: Int get() = achievements.count { it.unlocked }
}

internal const val TOP_DESTINATIONS_LIMIT = 3

private const val LONG_HAUL_KM = 300.0
private const val EXPLORER_DESTINATIONS = 5
private const val FREQUENT_TRIPS = 10
private const val FULL_GARAGE = 3

/** Everything the profile shows, derived from the rider's trips and garage size. */
internal fun profileStatsOf(
    trips: List<TripResponseDTO>,
    motorcycleCount: Int
): ProfileStats {
    val completed = trips.filter { it.status == "COMPLETED" }
    val totalKm = completed.sumOf { it.distanceKm }
    val destinations = visitsByDestination(completed)

    return ProfileStats(
        completedTrips = completed.size,
        totalKm = totalKm,
        destinationCount = destinations.size,
        topDestinations = destinations.take(TOP_DESTINATIONS_LIMIT),
        achievements = listOf(
            Achievement(
                AchievementKind.FIRST_TRIP,
                "Primeira viagem",
                "Conclua sua primeira viagem",
                completed.size.toDouble(),
                1.0
            ),
            Achievement(
                AchievementKind.TEN_TRIPS,
                "Viajante frequente",
                "Conclua 10 viagens",
                completed.size.toDouble(),
                FREQUENT_TRIPS.toDouble()
            ),
            Achievement(
                AchievementKind.KM_100,
                "Primeiros 100 km",
                "Rode 100 km em viagens concluídas",
                totalKm,
                100.0
            ),
            Achievement(
                AchievementKind.KM_1000,
                "Mil quilômetros",
                "Rode 1.000 km em viagens concluídas",
                totalKm,
                1_000.0
            ),
            Achievement(
                AchievementKind.KM_10000,
                "Lenda da estrada",
                "Rode 10.000 km em viagens concluídas",
                totalKm,
                10_000.0
            ),
            Achievement(
                AchievementKind.EXPLORER,
                "Explorador",
                "Visite $EXPLORER_DESTINATIONS destinos diferentes",
                destinations.size.toDouble(),
                EXPLORER_DESTINATIONS.toDouble()
            ),
            Achievement(
                AchievementKind.LONG_HAUL,
                "Longa distância",
                "Conclua uma viagem de ${LONG_HAUL_KM.toInt()} km ou mais",
                completed.maxOfOrNull { it.distanceKm } ?: 0.0,
                LONG_HAUL_KM
            ),
            Achievement(
                AchievementKind.OFF_ROAD,
                "Aventureiro",
                "Conclua uma viagem off-road",
                completed.count { it.terrain == "OFF_ROAD" }.toDouble(),
                1.0
            ),
            Achievement(
                AchievementKind.GARAGE,
                "Garagem cheia",
                "Cadastre $FULL_GARAGE motos",
                motorcycleCount.toDouble(),
                FULL_GARAGE.toDouble()
            )
        )
    )
}

/**
 * Completed trips grouped by destination (ignoring case and extra spaces), most
 * visited first; ties go to the one visited most recently.
 */
internal fun visitsByDestination(
    completedTrips: List<TripResponseDTO>
): List<DestinationVisit> {
    data class Group(
        val trips: List<TripResponseDTO>,
        val lastVisit: LocalDate
    )

    return completedTrips
        .filter { it.destination.isNotBlank() }
        .groupBy { it.destination.normalizedDestination().lowercase() }
        .map { (_, trips) ->
            val latest = trips.maxWithOrNull(
                compareBy<TripResponseDTO> { it.date() }.thenBy { it.id }
            )!!

            Group(trips, latest.date()) to DestinationVisit(
                name = latest.destination.normalizedDestination(),
                visits = trips.size,
                totalKm = trips.sumOf { it.distanceKm }
            )
        }
        .sortedWith(
            compareByDescending<Pair<Group, DestinationVisit>> { it.second.visits }
                .thenByDescending { it.first.lastVisit }
                .thenBy { it.second.name }
        )
        .map { it.second }
}

private fun String.normalizedDestination(): String =
    trim().replace(Regex("\\s+"), " ")

private fun TripResponseDTO.date(): LocalDate =
    runCatching { LocalDate.parse(tripDate) }.getOrDefault(LocalDate.MIN)
