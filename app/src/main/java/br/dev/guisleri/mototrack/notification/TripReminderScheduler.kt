package br.dev.guisleri.mototrack.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal const val REMINDER_CHANNEL_ID = "trip_reminders"

/** Whether the app is allowed to show notifications (always true before Android 13). */
fun canPostNotifications(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

/**
 * Keeps the trip reminders armed on the device.
 *
 * Alarms do not survive a reboot, so what is scheduled is also saved and
 * [rearm] puts it back from the boot receiver. The alarms are inexact on
 * purpose: a reminder a few minutes late is fine and needs no special permission.
 */
class TripReminderScheduler(context: Context) {

    private val context = context.applicationContext

    private val preferences =
        this.context.getSharedPreferences("trip_reminders", Context.MODE_PRIVATE)

    private val alarmManager =
        this.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val serializer = ListSerializer(TripReminder.serializer())

    var isEnabled: Boolean
        get() = preferences.getBoolean(KEY_ENABLED, false)
        set(value) {
            preferences.edit().putBoolean(KEY_ENABLED, value).apply()
            if (!value) cancelAll()
        }

    /**
     * Makes the armed reminders that follow the list of trips exactly [reminders] (or
     * none, when reminders are off). The ones scheduled by an event are left alone.
     */
    fun sync(reminders: List<TripReminder>) {
        if (!isEnabled) {
            cancelAll()
            return
        }

        val wanted = reminders.associateBy { it.requestCode }
        val current = saved()

        current
            .filter { it.kind.followsTripList && it.requestCode !in wanted }
            .forEach(::cancel)
        wanted.values.forEach(::schedule)
        save(wanted.values.toList() + current.filter { !it.kind.followsTripList })
    }

    /** Arms one reminder in answer to an event, replacing the one it repeats. */
    fun scheduleOne(reminder: TripReminder) {
        if (!isEnabled) return

        schedule(reminder)
        save(saved().filter { it.requestCode != reminder.requestCode } + reminder)
    }

    /** Disarms the reminder of [kind] for [tripId], if there is one. */
    fun cancelOne(tripId: Long, kind: ReminderKind) {
        val (matching, rest) = saved().partition { it.tripId == tripId && it.kind == kind }

        if (matching.isEmpty()) return

        matching.forEach(::cancel)
        save(rest)
    }

    /** Arms again what was saved, e.g. after the device restarted. */
    fun rearm() {
        if (!isEnabled) return

        val now = System.currentTimeMillis()
        val pending = saved().filter { it.triggerAtMillis > now }

        pending.forEach(::schedule)
        save(pending)
    }

    fun cancelAll() {
        saved().forEach(::cancel)
        save(emptyList())
    }

    /** Called when a reminder fires, so it is not armed again after a reboot. */
    internal fun markDelivered(requestCode: Int) {
        save(saved().filter { it.requestCode != requestCode })
    }

    private fun schedule(reminder: TripReminder) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.triggerAtMillis,
            pendingIntent(reminder.requestCode, reminder)
        )
    }

    private fun cancel(reminder: TripReminder) {
        alarmManager.cancel(pendingIntent(reminder.requestCode, reminder))
    }

    // Extras do not take part in matching, so the same request code finds the same alarm.
    private fun pendingIntent(requestCode: Int, reminder: TripReminder): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, TripReminderReceiver::class.java).apply {
                putExtra(TripReminderReceiver.EXTRA_TRIP_ID, reminder.tripId)
                putExtra(TripReminderReceiver.EXTRA_REQUEST_CODE, requestCode)
                putExtra(TripReminderReceiver.EXTRA_TITLE, reminder.title)
                putExtra(TripReminderReceiver.EXTRA_TEXT, reminder.text)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun saved(): List<TripReminder> {
        val json = preferences.getString(KEY_SCHEDULED, null) ?: return emptyList()

        return runCatching { Json.decodeFromString(serializer, json) }
            .getOrDefault(emptyList())
    }

    private fun save(reminders: List<TripReminder>) {
        preferences.edit()
            .putString(KEY_SCHEDULED, Json.encodeToString(serializer, reminders))
            .apply()
    }

    private companion object {
        const val KEY_ENABLED = "enabled"
        const val KEY_SCHEDULED = "scheduled"
    }
}

internal fun ensureReminderChannel(context: Context) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    manager.createNotificationChannel(
        NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Lembretes de viagem",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos da véspera e do dia das viagens, de viagens que seguem " +
                "em andamento e de gastos a registrar"
        }
    )
}
