package com.odorik.odorikbuddy.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.repository.UpdateRepository
import com.odorik.odorikbuddy.model.AppUpdateInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateWorkManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateRepository: UpdateRepository,
    private val appPreferences: AppPreferences
) {
    private val workManager by lazy { WorkManager.getInstance(context) }

    companion object {
        private const val UPDATE_CHECK_WORK_NAME = "update_check_work"
    }

    fun scheduleUpdateCheck() {
        if (!isAutoUpdateEnabled()) return

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val updateWorkRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            7, TimeUnit.DAYS // Weekly
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS) // Start after 1 hour
            .build()

        workManager.enqueueUniquePeriodicWork(
            UPDATE_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
            updateWorkRequest
        )
    }

    fun cancelUpdateCheck() {
        workManager.cancelUniqueWork(UPDATE_CHECK_WORK_NAME)
    }

    fun setAutoUpdateEnabled(enabled: Boolean) {
        appPreferences.autoUpdateEnabled = enabled

        if (enabled) {
            scheduleUpdateCheck()
        } else {
            cancelUpdateCheck()
        }
    }

    fun isAutoUpdateEnabled(): Boolean {
        return appPreferences.autoUpdateEnabled
    }

    fun performImmediateUpdateCheck() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check for updates
                val result = updateRepository.getAppUpdateInfo()

                result.onSuccess { updateInfo ->
                    // Cache the update info
                    updateRepository.getCachedUpdateInfo() // This will cache it

                    // Check if this is a new update that we haven't notified about
                    if (com.odorik.odorikbuddy.util.VersionUtils.isNewUpdateAvailable(updateInfo.version)) {
                        showImmediateUpdateNotification(updateInfo)
                        // Mark this version as notified
                        markVersionAsNotified(updateInfo.version)
                    }
                }
                // Ignore failures for immediate check - don't want to bother user
            } catch (e: Exception) {
                // Silently fail for immediate check
            }
        }
    }

    private fun markVersionAsNotified(version: String) {
        val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("last_notification_version", version).apply()
    }

    private fun showImmediateUpdateNotification(updateInfo: AppUpdateInfo) {
        // Check if POST_NOTIFICATIONS permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return // Permission not granted, skip notification
            }
        }

        // Create notification channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "update_notifications",
                context.getString(com.odorik.odorikbuddy.R.string.update_notifications),
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(com.odorik.odorikbuddy.R.string.update_notifications_description)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open the app
        val intent = android.content.Intent(context, com.odorik.odorikbuddy.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, "update_notifications")
            .setSmallIcon(com.odorik.odorikbuddy.R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(com.odorik.odorikbuddy.R.string.app_update_available))
            .setContentText(context.getString(com.odorik.odorikbuddy.R.string.new_version_available, updateInfo.version))
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle()
                .bigText(updateInfo.message))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            androidx.core.app.NotificationManagerCompat.from(context).notify(1002, notification)
        } catch (e: SecurityException) {
            // Permission not granted, silently fail
        }
    }
}