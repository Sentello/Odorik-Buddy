package com.odorik.odorikbuddy.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.*
import com.odorik.odorikbuddy.BuildConfig
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
    private val updateRepository: UpdateRepository
) {
    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val UPDATE_CHECK_WORK_NAME = "update_check_work"
        private const val PREF_AUTO_UPDATE_ENABLED = "auto_update_enabled"
        private const val PREF_AUTO_UPDATE_DEFAULT = false
    }

    fun scheduleUpdateCheck() {
        if (!isAutoUpdateEnabled()) return

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val updateWorkRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            7, TimeUnit.DAYS 
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS) 
            .build()

        workManager.enqueueUniquePeriodicWork(
            UPDATE_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, 
            updateWorkRequest
        )
    }

    fun cancelUpdateCheck() {
        workManager.cancelUniqueWork(UPDATE_CHECK_WORK_NAME)
    }

    fun setAutoUpdateEnabled(enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREF_AUTO_UPDATE_ENABLED, enabled).apply()

        if (enabled) {
            scheduleUpdateCheck()
        } else {
            cancelUpdateCheck()
        }
    }

    fun isAutoUpdateEnabled(): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean(PREF_AUTO_UPDATE_ENABLED, PREF_AUTO_UPDATE_DEFAULT)
    }

    fun performImmediateUpdateCheck() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                
                val result = updateRepository.getAppUpdateInfo()

                result.onSuccess { updateInfo ->
                    
                    updateRepository.getCachedUpdateInfo() 

                    
            } catch (e: Exception) {
                
            }
        }
    }

    private fun isNewUpdateAvailable(updateInfo: AppUpdateInfo): Boolean {
        val currentVersion = BuildConfig.VERSION_NAME
        val latestVersion = updateInfo.version

        
        return try {
            val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
            val latestParts = latestVersion.split(".").map { it.toIntOrNull() ?: 0 }

            for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
                val current = currentParts.getOrNull(i) ?: 0
                val latest = latestParts.getOrNull(i) ?: 0
                if (latest > current) return true
                if (latest < current) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun markVersionAsNotified(version: String) {
        val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("last_notification_version", version).apply()
    }

    private fun showImmediateUpdateNotification(updateInfo: AppUpdateInfo) {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return 
            }
        }

        
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
            
        }
    }
}