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

/**
 * Fetches the credit and writes it into the balance widgets' state. Shared by the tap
 * action, the config screen, the periodic worker and the Dashboard.
 *
 * "Last updated" only moves when a balance was actually fetched, so an old balance never
 * looks fresh after a failed refresh.
 */
@Singleton
class BalanceWidgetRefresher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCreditUseCase: GetCreditUseCase
) {

    /**
     * Refreshes one widget. With [showProgress] (the user tapped the widget or saved its
     * settings) the widget shows "…" while loading and an error if the fetch fails.
     */
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
            // Offline the fetch fails within a few ms. Two widget updates that close together,
            // while a Glance session is active (e.g. right after the widget was configured),
            // lose the second one and the widget stays on "…". Keep "…" up for a moment.
            val shownFor = SystemClock.elapsedRealtime() - progressShownAt
            if (shownFor < MIN_PROGRESS_MS) delay(MIN_PROGRESS_MS - shownFor)
        }
        writeResult(glanceId, result, showError = showProgress)
    }

    /**
     * Background refresh of every balance widget: one API call, written to all of them.
     * Failures are silent and keep the old balance and its time.
     */
    suspend fun refreshAll() {
        val glanceIds = balanceWidgetIds()
        if (glanceIds.isEmpty()) return

        val result = getCreditUseCase.execute()
        glanceIds.forEach { writeResult(it, result, showError = false) }
    }

    /** Writes a balance the app already fetched (Dashboard) into every widget. No network call. */
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
            // Best effort: a widget that was just removed must not break the others.
            Log.w(TAG, "Failed to update balance widget $glanceId", e)
        }
    }

    private companion object {
        const val TAG = "BalanceWidgetRefresher"
        const val MIN_PROGRESS_MS = 700L
    }
}
