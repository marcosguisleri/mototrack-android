package br.dev.guisleri.mototrack.notification

import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.formatDistance
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private val EVE_REMINDER_TIME = LocalTime.of(18, 0)
private val DAY_REMINDER_TIME = LocalTime.of(7, 30)

/** A trip still open this long after it started is probably one that was never closed. */
private val FORGOTTEN_AFTER = Duration.ofHours(12)

/** How long after finishing a trip without costs the rider is nudged to register them. */
private val COSTS_AFTER = Duration.ofHours(1)

// Request codes of the evening and morning reminders are the ones armed on devices
// already, so the new kinds live in ranges of their own.
private const val FORGOTTEN_CODE_BASE = 2_000_000
private const val COSTS_CODE_BASE = 3_000_000

enum class ReminderKind {
    /** The evening before the trip. */
    EVE,

    /** The morning of the trip. */
    DAY,

    /** A trip that has been in progress for a long time. */
    FORGOTTEN,

    /** A finished trip with nothing registered in its costs. */
    COSTS;

    /**
     * Whether the reminders of this kind are derived from the list of trips (and so
     * re-planned with it) rather than scheduled by the event they follow.
     */
    val followsTripList: Boolean get() = this != COSTS
}

@Serializable
data class TripReminder(
    val tripId: Long,
    val kind: ReminderKind,
    val triggerAtMillis: Long,
    val title: String,
    val text: String
) {
    /** Stable per trip and kind, so the same reminder can be replaced or cancelled. */
    val requestCode: Int
        get() = when (kind) {
            ReminderKind.EVE, ReminderKind.DAY -> (tripId * 2 + kind.ordinal).toInt()
            ReminderKind.FORGOTTEN -> FORGOTTEN_CODE_BASE + tripId.toInt()
            ReminderKind.COSTS -> COSTS_CODE_BASE + tripId.toInt()
        }
}

private fun routeOf(trip: TripResponseDTO): String =
    "${trip.origin} → ${trip.destination} · ${formatDistance(trip.distanceKm)}"

/**
 * The nudge for a trip in progress: it fires [FORGOTTEN_AFTER] after the start, and
 * only if that is still ahead (a rider who opens the app later is not forgetting).
 */
fun forgottenTripReminder(
    trip: TripResponseDTO,
    nowMillis: Long
): TripReminder? {
    if (trip.status != "IN_PROGRESS") return null

    val startedAt = runCatching { Instant.parse(trip.startedAt) }.getOrNull() ?: return null
    val trigger = startedAt.plus(FORGOTTEN_AFTER).toEpochMilli()

    if (trigger <= nowMillis) return null

    return TripReminder(
        tripId = trip.id,
        kind = ReminderKind.FORGOTTEN,
        triggerAtMillis = trigger,
        title = "Sua viagem ainda está em andamento",
        text = "${routeOf(trip)}. Já chegou? Conclua a viagem para guardar o resumo."
    )
}

/** The nudge to register what a finished trip cost, an hour after finishing it. */
fun costsReminder(
    trip: TripResponseDTO,
    nowMillis: Long
): TripReminder =
    TripReminder(
        tripId = trip.id,
        kind = ReminderKind.COSTS,
        triggerAtMillis = nowMillis + COSTS_AFTER.toMillis(),
        title = "Quanto custou essa viagem?",
        text = "${routeOf(trip)}. Registre abastecimentos e pedágios e veja o custo por km."
    )

/**
 * The reminders to schedule for the rider's trips: one the evening before and one
 * the morning of every planned trip, and a nudge for a trip left in progress for
 * too long, leaving out the ones whose time has passed.
 */
fun planTripReminders(
    trips: List<TripResponseDTO>,
    nowMillis: Long,
    zone: ZoneId = ZoneId.systemDefault()
): List<TripReminder> {
    val forgotten = trips.mapNotNull { forgottenTripReminder(it, nowMillis) }

    val planned = trips
        .filter { it.status == "PLANNED" }
        .flatMap { trip ->
            val date = runCatching { LocalDate.parse(trip.tripDate) }.getOrNull()
                ?: return@flatMap emptyList()
            val route = routeOf(trip)

            listOf(
                TripReminder(
                    tripId = trip.id,
                    kind = ReminderKind.EVE,
                    triggerAtMillis = date.minusDays(1).atTime(EVE_REMINDER_TIME)
                        .atZone(zone).toInstant().toEpochMilli(),
                    title = "Sua viagem é amanhã",
                    text = "$route. Confira a moto e prepare a bagagem!"
                ),
                TripReminder(
                    tripId = trip.id,
                    kind = ReminderKind.DAY,
                    triggerAtMillis = date.atTime(DAY_REMINDER_TIME)
                        .atZone(zone).toInstant().toEpochMilli(),
                    title = "Hoje é dia de viajar",
                    text = "$route. Boa viagem!"
                )
            )
        }

    return (planned + forgotten)
        .filter { it.triggerAtMillis > nowMillis }
        .sortedBy { it.triggerAtMillis }
}
