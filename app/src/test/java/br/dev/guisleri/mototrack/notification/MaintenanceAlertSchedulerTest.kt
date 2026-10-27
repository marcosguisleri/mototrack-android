package br.dev.guisleri.mototrack.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class MaintenanceAlertSchedulerTest {

    private val zone = ZoneId.of("America/Sao_Paulo")

    private fun at(hour: Int, minute: Int = 0, daysFromToday: Long = 0): Long =
        LocalDate.now(zone).plusDays(daysFromToday).atTime(hour, minute)
            .atZone(zone).toInstant().toEpochMilli()

    @Test
    fun beforeNineTheCheckIsTodayAtNine() {
        assertEquals(at(9), nextCheckMillis(at(7), zone))
    }

    @Test
    fun afterNineTheCheckIsTomorrowAtNine() {
        assertEquals(at(9, daysFromToday = 1), nextCheckMillis(at(9, 30), zone))
        assertEquals(at(9, daysFromToday = 1), nextCheckMillis(at(9), zone))
    }

    @Test
    fun theCheckIsAlwaysInTheFuture() {
        val now = at(15)

        assertTrue(nextCheckMillis(now, zone) > now)
    }
}
