package com.odorik.odorikbuddy.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.odorik.odorikbuddy.ui.widget.BalanceWidgetRefresher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class BalanceWidgetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val balanceWidgetRefresher: BalanceWidgetRefresher
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            balanceWidgetRefresher.refreshAll()
        } catch (e: Exception) {
            android.util.Log.w("BalanceWidgetWorker", "Balance widget refresh failed", e)
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "balance_widget_refresh"

        private const val INTERVAL_HOURS = 1L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<BalanceWidgetWorker>(
                INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_NAME)
        }
    }
}
