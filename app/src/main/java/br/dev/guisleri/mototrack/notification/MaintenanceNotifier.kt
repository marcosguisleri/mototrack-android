package br.dev.guisleri.mototrack.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.dev.guisleri.mototrack.MainActivity
import br.dev.guisleri.mototrack.R
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceAlert
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceLevel
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceStore
import br.dev.guisleri.mototrack.data.maintenance.planMaintenanceAlerts
import java.time.LocalDate

internal const val MAINTENANCE_CHANNEL_ID = "maintenance_alerts"

private const val MAINTENANCE_NOTIFICATION_BASE = 5_000_000

/**
 * Tells the rider when an item of a motorcycle's maintenance is coming up or overdue.
 * It works from what is saved on the device, so it runs with the app closed.
 */
class MaintenanceNotifier(
    context: Context,
    private val store: MaintenanceStore,
    private val today: () -> LocalDate = { LocalDate.now() }
) {

    private val context = context.applicationContext

    /** Notifies about what became due since the last check; one notification per motorcycle. */
    suspend fun check() {
        if (!TripReminderScheduler(context).isEnabled || !canPostNotifications(context)) return

        store.all().forEach { (motorcycleId, maintenance) ->
            val plan = planMaintenanceAlerts(maintenance, today())

            if (plan.alerts.isNotEmpty()) {
                notify(motorcycleId, maintenance.motorcycleName, plan.alerts)
            }

            if (plan.alerted != maintenance.alerted) {
                // Only the told-about marks are written, so a change made meanwhile is kept.
                store.update(motorcycleId) { it.copy(alerted = plan.alerted) }
            }
        }
    }

    private fun notify(motorcycleId: Long, name: String?, alerts: List<MaintenanceAlert>) {
        ensureMaintenanceChannel(context)

        val title = name?.let { "Manutenção da $it" } ?: "Manutenção da sua moto"
        val text = alerts.joinToString(" · ") { alert ->
            "${alert.item.label}: ${if (alert.level == MaintenanceLevel.OVERDUE) "vencida" else "chegando"}"
        }

        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MAINTENANCE_CHANNEL_ID)
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
            NotificationManagerCompat.from(context).notify(
                MAINTENANCE_NOTIFICATION_BASE + (motorcycleId % 1_000_000).toInt(),
                notification
            )
        } catch (exception: SecurityException) {
            // The permission was revoked between the check and now.
        }
    }
}

internal fun ensureMaintenanceChannel(context: Context) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    manager.createNotificationChannel(
        NotificationChannel(
            MAINTENANCE_CHANNEL_ID,
            "Manutenção da moto",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos quando óleo, corrente, pneus e outros itens estão chegando ou vencidos"
        }
    )
}
