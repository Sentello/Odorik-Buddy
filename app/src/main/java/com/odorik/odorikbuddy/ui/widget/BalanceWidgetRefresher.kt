package com.odorik.odorikbuddy.ui.widget

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceWidgetRefresher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCreditUseCase: GetCreditUseCase
) {

    suspend fun refresh(glanceId: GlanceId, showProgress: Boolean) {
        val progressShownAt = SystemClock.elapsedRealtime()
        if (showProgress) {
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[BalanceWidget.isLoadingKey] = true
                prefs.remove(BalanceWidget.errorKey)
            }
            BalanceWidget().update(context, glanceId)
        }

        val result = getCreditUseCase.execute()
        if (showProgress) {
            val shownFor = SystemClock.elapsedRealtime() - progressShownAt
            if (shownFor < MIN_PROGRESS_MS) delay(MIN_PROGRESS_MS - shownFor)
        }
        writeResult(glanceId, result, showError = showProgress)
    }

    suspend fun refreshAll() {
        val glanceIds = balanceWidgetIds()
        if (glanceIds.isEmpty()) return

        val result = getCreditUseCase.execute()
        glanceIds.forEach { writeResult(it, result, showError = false) }
    }

    suspend fun pushBalance(balance: Double) {
        balanceWidgetIds().forEach { writeResult(it, Result.success(balance), showError = false) }
    }

    private suspend fun balanceWidgetIds(): List<GlanceId> = try {
        GlanceAppWidgetManager(context).getGlanceIds(BalanceWidget::class.java)
    } catch (e: Exception) {
        Log.w(TAG, "Failed to list balance widgets", e)
        emptyList()
    }

    private suspend fun writeResult(glanceId: GlanceId, result: Result<Double>, showError: Boolean) {
        try {
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[BalanceWidget.isLoadingKey] = false
                result.onSuccess { balance ->
                    prefs[BalanceWidget.balanceKey] = balance
                    prefs[BalanceWidget.lastUpdatedKey] = System.currentTimeMillis()
                    prefs.remove(BalanceWidget.errorKey)
                }.onFailure { error ->
                    if (showError) {
                        prefs[BalanceWidget.errorKey] = error.message
                            ?: context.getString(R.string.error_unknown_generic)
                    }
                }
            }
            BalanceWidget().update(context, glanceId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update balance widget $glanceId", e)
        }
    }

    private companion object {
        const val TAG = "BalanceWidgetRefresher"
        const val MIN_PROGRESS_MS = 700L
    }
}
