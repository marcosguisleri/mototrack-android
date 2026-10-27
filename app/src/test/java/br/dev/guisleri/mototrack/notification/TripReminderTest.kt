package br.dev.guisleri.mototrack.notification

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class TripReminderTest {

    private val zone = ZoneId.of("America/Sao_Paulo")

    private fun millis(dateTime: String) =
        LocalDateTime.parse(dateTime).atZone(zone).toInstant().toEpochMilli()

    private fun trip(
        id: Long = 1,
        date: String,
        status: String = "PLANNED"
    ) = TripResponseDTO(
        id = id, origin = "São Paulo", destination = "Santos", distanceKm = 70.0,
        status = status, terrain = "ASPHALT", tripDate = date,
        motorcycle = MotorcycleResponseDTO(
            id = 1, brand = "Honda", model = "CB 500X", color = "Preta", year = 2024,
            engineCapacity = 500, owner = MotorcycleOwnerDTO(1, "Rider", "rider@example.com")
        )
    )

    @Test
    fun plannedTripGetsAnEveningAndAMorningReminder() {
        val reminders = planTripReminders(
            listOf(trip(date = "2026-10-25")), millis("2026-10-21T10:00:00"), zone
        )

        assertEquals(
            listOf(millis("2026-10-24T18:00:00"), millis("2026-10-25T07:30:00")),
            reminders.map { it.triggerAtMillis }
        )
        assertEquals(listOf(ReminderKind.EVE, ReminderKind.DAY), reminders.map { it.kind })
        assertEquals("Sua viagem é amanhã", reminders[0].title)
        assertTrue(reminders[0].text.startsWith("São Paulo → Santos · 70 km"))
    }

    @Test
    fun onlyPlannedTripsAreRemindedOf() {
        val reminders = planTripReminders(
            listOf(
                trip(1, "2026-10-25", "IN_PROGRESS"),
                trip(2, "2026-10-26", "COMPLETED"),
                trip(3, "2026-10-27", "PLANNED")
            ),
            millis("2026-10-21T10:00:00"), zone
        )

        assertEquals(setOf(3L), reminders.map { it.tripId }.toSet())
    }

    @Test
    fun remindersThatAlreadyPassedAreLeftOut() {
        val reminders = planTripReminders(
            listOf(trip(date = "2026-10-22")), millis("2026-10-21T20:00:00"), zone
        )

        assertEquals(listOf(ReminderKind.DAY), reminders.map { it.kind })
    }

    @Test
    fun tripsInThePastOrToday_afterTheMorningReminder_getNothing() {
        val reminders = planTripReminders(
            listOf(trip(1, "2026-10-21"), trip(2, "2026-10-10")),
            millis("2026-10-21T09:00:00"), zone
        )

        assertTrue(reminders.isEmpty())
    }

    @Test
    fun invalidDatesAreIgnored() {
        assertTrue(planTripReminders(listOf(trip(date = "amanhã")), 0L, zone).isEmpty())
    }

    @Test
    fun requestCodesAreUniquePerTripAndKind() {
        val reminders = planTripReminders(
            listOf(trip(1, "2026-10-25"), trip(2, "2026-10-26")), 0L, zone
        )

        assertEquals(4, reminders.map { it.requestCode }.toSet().size)
    }

    @Test
    fun remindersAreSortedByTime() {
        val reminders = planTripReminders(
            listOf(trip(1, "2026-10-30"), trip(2, "2026-10-25")), 0L, zone
        )

        assertEquals(reminders.sortedBy { it.triggerAtMillis }, reminders)
    }

    private fun tripInProgress(id: Long = 1, startedAt: String?) =
        trip(id = id, date = "2026-10-22", status = "IN_PROGRESS").copy(startedAt = startedAt)

    @Test
    fun aTripInProgressIsRemindedOfTwelveHoursAfterItStarted() {
        val reminder = forgottenTripReminder(
            tripInProgress(startedAt = "2026-10-22T08:00:00Z"),
            nowMillis = millis("2026-10-22T09:00:00")
        )

        assertEquals(ReminderKind.FORGOTTEN, reminder!!.kind)
        assertEquals(
            java.time.Instant.parse("2026-10-22T20:00:00Z").toEpochMilli(),
            reminder.triggerAtMillis
        )
        assertTrue(reminder.text.startsWith("São Paulo → Santos · 70 km"))
    }

    @Test
    fun noForgottenReminderOncePastOrWithoutAStart() {
        val started = "2026-10-22T08:00:00Z"
        val afterTwelveHours = java.time.Instant.parse("2026-10-22T21:00:00Z").toEpochMilli()

        assertNull(forgottenTripReminder(tripInProgress(startedAt = started), afterTwelveHours))
        assertNull(forgottenTripReminder(tripInProgress(startedAt = null), 0L))
        assertNull(
            forgottenTripReminder(
                trip(date = "2026-10-22", status = "COMPLETED").copy(startedAt = started), 0L
            )
        )
    }

    @Test
    fun theForgottenReminderIsPartOfTheTripPlan() {
        val reminders = planTripReminders(
            listOf(tripInProgress(startedAt = "2026-10-22T08:00:00Z")),
            nowMillis = millis("2026-10-22T09:00:00"), zone
        )

        assertEquals(listOf(ReminderKind.FORGOTTEN), reminders.map { it.kind })
    }

    @Test
    fun theCostsReminderComesAnHourAfterTheTripEnds() {
        val reminder = costsReminder(
            trip(date = "2026-10-22", status = "COMPLETED"),
            nowMillis = 1_000_000L
        )

        assertEquals(ReminderKind.COSTS, reminder.kind)
        assertEquals(1_000_000L + 3_600_000L, reminder.triggerAtMillis)
        assertTrue(!ReminderKind.COSTS.followsTripList)
        assertTrue(ReminderKind.FORGOTTEN.followsTripList)
    }

    @Test
    fun newKindsNeverReuseTheCodesOfEveningAndMorningReminders() {
        val codes = listOf(
            TripReminder(7, ReminderKind.EVE, 0, "", ""),
            TripReminder(7, ReminderKind.DAY, 0, "", ""),
            TripReminder(7, ReminderKind.FORGOTTEN, 0, "", ""),
            TripReminder(7, ReminderKind.COSTS, 0, "", "")
        ).map { it.requestCode }

        assertEquals(4, codes.toSet().size)
        // The first two are the ones already armed on devices.
        assertEquals(listOf(14, 15), codes.take(2))
    }
}
