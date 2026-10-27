package br.dev.guisleri.mototrack.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.dev.guisleri.mototrack.MainActivity
import br.dev.guisleri.mototrack.R

/** Shows a trip reminder when its alarm goes off. */
class TripReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val text = intent.getStringExtra(EXTRA_TEXT) ?: return

        TripReminderScheduler(context).markDelivered(requestCode)

        if (!canPostNotifications(context)) return

        ensureReminderChannel(context)

        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_mototrack)
            .setColor(0xFFF26A1B.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(requestCode, notification)
        } catch (exception: SecurityException) {
            // The permission was revoked between the check above and now.
        }
    }

    companion object {
        const val EXTRA_TRIP_ID = "trip_id"
        const val EXTRA_REQUEST_CODE = "request_code"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
    }
}

/** Alarms are lost when the device restarts or the app is updated; this puts them back. */
class TripReminderBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            TripReminderScheduler(context).rearm()

            if (TripReminderScheduler(context).isEnabled) {
                MaintenanceAlertScheduler(context).ensureScheduled()
            }
        }
    }
}
