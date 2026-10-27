package br.dev.guisleri.mototrack.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.dev.guisleri.mototrack.data.maintenance.DataStoreMaintenanceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private val CHECK_TIME = LocalTime.of(9, 0)
private const val CHECK_REQUEST_CODE = 6_000_001

/** Looks at every motorcycle's maintenance once a day, at 9h, even if the app is closed. */
class MaintenanceAlertScheduler(context: Context) {

    private val context = context.applicationContext

    private val alarmManager =
        this.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Arms the next daily check (the receiver arms the one after it). */
    fun ensureScheduled(
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextCheckMillis(now, zone),
            pendingIntent()
        )
    }

    fun cancel() {
        alarmManager.cancel(pendingIntent())
    }

    private fun pendingIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            CHECK_REQUEST_CODE,
            Intent(context, MaintenanceAlertReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}

/** The next 9h strictly after [nowMillis]. */
fun nextCheckMillis(nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
    val today = LocalDate.now(zone).atTime(CHECK_TIME).atZone(zone).toInstant().toEpochMilli()

    return if (today > nowMillis) {
        today
    } else {
        LocalDate.now(zone).plusDays(1).atTime(CHECK_TIME).atZone(zone).toInstant().toEpochMilli()
    }
}

/** Runs the daily check and arms the next one. */
class MaintenanceAlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                MaintenanceNotifier(context, DataStoreMaintenanceStore(context)).check()
            } finally {
                if (TripReminderScheduler(context).isEnabled) {
                    MaintenanceAlertScheduler(context).ensureScheduled()
                }
                pending.finish()
            }
        }
    }
}
