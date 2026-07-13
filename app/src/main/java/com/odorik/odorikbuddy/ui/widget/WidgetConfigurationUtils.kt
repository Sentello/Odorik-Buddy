package com.odorik.odorikbuddy.ui.widget

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Small shared helpers to reduce boilerplate in widget configuration activities.
 */
object WidgetConfigurationUtils {

    fun updateWidgetStateAndRefresh(
        context: Context,
        appWidgetId: Int,
        glanceAppWidget: GlanceAppWidget,
        scope: CoroutineScope,
        updateBlock: (MutablePreferences) -> Unit
    ) {
        scope.launch {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
            updateAppWidgetState(context, glanceId, updateBlock)
            glanceAppWidget.update(context, glanceId)
        }
    }

    /**
     * Convenience for the common "save config then update widget" pattern.
     */
    inline fun updatePreferences(
        prefs: MutablePreferences,
        crossinline block: MutablePreferences.() -> Unit
    ) {
        prefs.block()
    }
}
