package com.odorik.odorikbuddy.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.odorik.odorikbuddy.MainActivity
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LanguagePreferences
import com.odorik.odorikbuddy.model.AppUpdateInfo
import com.odorik.odorikbuddy.util.VersionUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single owner of the "update available" notification: version comparison,
 * once-per-version deduplication, channel creation, and posting.
 * Used by both [UpdateCheckWorker] (periodic) and [UpdateWorkManager] (immediate check).
 */
@Singleton
class UpdateNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "update_notifications"
        const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "update_prefs"
        private const val KEY_LAST_NOTIFICATION_VERSION = "last_notification_version"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Shows the update notification at most once per remote version.
     * No-op when the version is not newer than the installed app, when the user
     * was already notified about it, or when notifications are not permitted.
     */
    fun notifyIfNeeded(updateInfo: AppUpdateInfo) {
        if (!VersionUtils.isNewUpdateAvailable(updateInfo.version)) return
        if (prefs.getString(KEY_LAST_NOTIFICATION_VERSION, null) == updateInfo.version) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Do not mark as notified — retry once the user grants the permission.
            return
        }

        createNotificationChannel()

        // Tapping goes straight to the download page; without a safe link, open the app.
        val url = updateInfo.safeDownloadUrl()
        val intent = if (url != null) {
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        } else {
            Intent(context, MainActivity::class.java)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val language = LanguagePreferences.getPreferredLanguage(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_buddy)
            .setContentTitle(context.getString(R.string.app_update_available))
            .setContentText(context.getString(R.string.new_version_available, updateInfo.version))
            .setStyle(NotificationCompat.BigTextStyle().bigText(updateInfo.localizedMessage(language)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            prefs.edit().putString(KEY_LAST_NOTIFICATION_VERSION, updateInfo.version).apply()
        } catch (e: SecurityException) {
            // Notifications revoked between the check and the post — try again next run.
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.update_notifications),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.update_notifications_description)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
