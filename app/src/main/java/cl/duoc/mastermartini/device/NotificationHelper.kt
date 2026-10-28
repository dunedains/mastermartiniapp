package cl.duoc.mastermartini.device

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.mastermartini.MainActivity
import cl.duoc.mastermartini.R

/**
 * RECURSO NATIVO 1 — Notificaciones locales.
 * - Crea el canal (obligatorio desde Android 8).
 * - Verifica el permiso POST_NOTIFICATIONS (obligatorio desde Android 13)
 *   antes de publicar; si no está concedido, no publica (no se cae la app).
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "mm_eventos"
    }

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.notification_channel_desc) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** true si la app puede mostrar notificaciones ahora mismo. */
    fun hasPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

    /** Devuelve true si la notificación se mostró. */
    @SuppressLint("MissingPermission") // el permiso se verifica en hasPermission()
    fun show(title: String, text: String): Boolean {
        if (!hasPermission()) return false
        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        return try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
