package com.odorik.odorikbuddy.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.odorik.odorikbuddy.worker.BalanceWidgetWorker

class BalanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BalanceWidget()

    /** First balance widget added. */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        BalanceWidgetWorker.schedule(context)
    }

    /**
     * Also runs for widgets that already existed before the background refresh: Android
     * sends an update after the app is updated and every updatePeriodMillis. Scheduling is
     * idempotent, so this is a no-op once the work exists.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        BalanceWidgetWorker.schedule(context)
    }

    /** Last balance widget removed. */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        BalanceWidgetWorker.cancel(context)
    }
}
