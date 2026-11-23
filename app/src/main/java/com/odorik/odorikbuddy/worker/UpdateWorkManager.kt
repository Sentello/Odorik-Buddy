package com.odorik.odorikbuddy.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.repository.UpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateWorkManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateRepository: UpdateRepository,
    private val updateNotifier: UpdateNotifier,
    private val appPreferences: AppPreferences
) {
    private val workManager by lazy { WorkManager.getInstance(context) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val UPDATE_CHECK_WORK_NAME = "update_check_work"
    }

    /**
     * Called from [android.app.Application.onCreate], so it must not touch disk on the main
     * thread: [isAutoUpdateEnabled] reads DataStore, which falls back to a blocking load if the
     * cache has not been warmed yet. Scheduling one week of work an instant later costs nothing,
     * so the whole body runs on IO.
     */
    fun scheduleUpdateCheck() {
        scope.launch {
            if (!isAutoUpdateEnabled()) return@launch

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val updateWorkRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                7, TimeUnit.DAYS // Weekly: releases are rare
            )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS) // Start after 1 hour
                .build()

            // UPDATE (not KEEP) so installs scheduled by an older version pick up a changed
            // interval or constraints. It keeps the original enqueue time, so starting the
            // app does not keep pushing the next check back.
            workManager.enqueueUniquePeriodicWork(
                UPDATE_CHECK_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                updateWorkRequest
            )
        }
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
        scope.launch {
            try {
                updateRepository.getAppUpdateInfo().onSuccess { updateInfo ->
                    updateNotifier.notifyIfNeeded(updateInfo)
                }
            } catch (e: Exception) {
                // Best-effort immediate check — never bother the user with failures
            }
        }
    }
}
