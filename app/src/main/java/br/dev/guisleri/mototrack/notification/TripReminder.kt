package br.dev.guisleri.mototrack.notification

import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.formatDistance
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private val EVE_REMINDER_TIME = LocalTime.of(18, 0)
private val DAY_REMINDER_TIME = LocalTime.of(7, 30)

enum class ReminderKind {
    /** The evening before the trip. */
    EVE,

    /** The morning of the trip. */
    DAY
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
        get() = (tripId * ReminderKind.entries.size + kind.ordinal).toInt()
}

/**
 * The reminders to schedule for the rider's trips: one the evening before and one
 * the morning of every planned trip, leaving out the ones whose time has passed.
 */
fun planTripReminders(
    trips: List<TripResponseDTO>,
    nowMillis: Long,
    zone: ZoneId = ZoneId.systemDefault()
): List<TripReminder> =
    trips
        .filter { it.status == "PLANNED" }
        .flatMap { trip ->
            val date = runCatching { LocalDate.parse(trip.tripDate) }.getOrNull()
                ?: return@flatMap emptyList()
            val route = "${trip.origin} → ${trip.destination} · ${formatDistance(trip.distanceKm)}"

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
        .filter { it.triggerAtMillis > nowMillis }
        .sortedBy { it.triggerAtMillis }
