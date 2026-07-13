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
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.odorik.odorikbuddy.MainActivity
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.repository.UpdateRepository
import com.odorik.odorikbuddy.model.AppUpdateInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateRepository: UpdateRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "update_notifications"
        const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "update_prefs"
        private const val KEY_LAST_UPDATE_INFO = "last_update_info"
        private const val KEY_LAST_NOTIFICATION_VERSION = "last_notification_version"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Check for updates
            val result = updateRepository.getAppUpdateInfo()

            result.onSuccess { updateInfo ->
                // Cache the update info
                saveUpdateInfo(updateInfo)

                // Check if this is a new update that we haven't notified about
                if (com.odorik.odorikbuddy.util.VersionUtils.isNewUpdateAvailable(updateInfo.version)) {
                    showUpdateNotification(updateInfo)
                    // Mark this version as notified
                    markVersionAsNotified(updateInfo.version)
                }
            }.onFailure {
                // Log error but don't fail the worker - network issues shouldn't break the periodic task
                // In a production app, you might want to implement retry logic or exponential backoff
            }

            Result.success()
        } catch (e: Exception) {
            // Handle unexpected errors
            Result.failure()
        }
    }

    private fun saveUpdateInfo(updateInfo: AppUpdateInfo) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = com.google.gson.Gson()
        val json = gson.toJson(updateInfo)
        prefs.edit().putString(KEY_LAST_UPDATE_INFO, json).apply()
    }

    private fun markVersionAsNotified(version: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_NOTIFICATION_VERSION, version).apply()
    }

    private fun showUpdateNotification(updateInfo: AppUpdateInfo) {
        // Check if POST_NOTIFICATIONS permission is granted (required for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, skip showing notification
                return
            }
        }

        createNotificationChannel()

        // Create intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(updateInfo.message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not granted, silently fail
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.update_notifications)
            val descriptionText = context.getString(R.string.update_notifications_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}