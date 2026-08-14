package com.odorik.odorikbuddy.worker

import android.Manifest
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
import com.odorik.odorikbuddy.MainActivity
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.model.AppUpdateInfo
import com.odorik.odorikbuddy.util.VersionUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


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


    fun notifyIfNeeded(updateInfo: AppUpdateInfo) {
        if (!VersionUtils.isNewUpdateAvailable(updateInfo.version)) return
        if (prefs.getString(KEY_LAST_NOTIFICATION_VERSION, null) == updateInfo.version) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_update_available))
            .setContentText(context.getString(R.string.new_version_available, updateInfo.version))
            .setStyle(NotificationCompat.BigTextStyle().bigText(updateInfo.message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            prefs.edit().putString(KEY_LAST_NOTIFICATION_VERSION, updateInfo.version).apply()
        } catch (e: SecurityException) {

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
