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

/**
 * Refreshes the balance widgets in the background. Scheduled while at least one balance
 * widget is on the home screen (see BalanceWidgetReceiver).
 */
@HiltWorker
class BalanceWidgetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val balanceWidgetRefresher: BalanceWidgetRefresher
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Always success: a failed fetch (offline, logged out) just waits for the next
        // period instead of retrying with backoff.
        try {
            balanceWidgetRefresher.refreshAll()
        } catch (e: Exception) {
            android.util.Log.w("BalanceWidgetWorker", "Balance widget refresh failed", e)
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "balance_widget_refresh"

        /** The balance only changes when you call or send SMS; hourly is fresh enough. */
        private const val INTERVAL_HOURS = 1L

        /** Idempotent: an already scheduled refresh is kept. */
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
