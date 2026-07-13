package com.odorik.odorikbuddy.ui.widget

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles refreshing home screen widgets when underlying data (Tiles) changes.
 * This makes the app feel much more connected — editing a tile in the Calls tab
 * immediately updates any widgets using that tile.
 */
@Singleton
class WidgetUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Refreshes all OdorikTileWidgets (quick dial) that are currently using the given tileId.
     * Safe to call even if no widgets exist.
     */
    fun refreshWidgetsUsingTile(tileId: Int) {
        scope.launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(OdorikTileWidget::class.java)

                for (glanceId in glanceIds) {
                    val prefs = getAppWidgetState(
                        context = context,
                        definition = PreferencesGlanceStateDefinition,
                        glanceId = glanceId
                    )
                    val widgetTileId = prefs[intPreferencesKey("tile_id")] ?: -1
                    if (widgetTileId == tileId) {
                        OdorikTileWidget().update(context, glanceId)
                    }
                }
            } catch (e: Exception) {
                // Best effort — don't crash the app if widget refresh fails
                android.util.Log.w("WidgetUpdateManager", "Failed to refresh widgets for tile $tileId", e)
            }
        }
    }

    /**
     * Refreshes ALL OdorikTileWidgets. Useful after bulk operations or when we don't know the exact tile.
     */
    fun refreshAllQuickDialWidgets() {
        scope.launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(OdorikTileWidget::class.java)
                val widget = OdorikTileWidget()
                glanceIds.forEach { glanceId ->
                    widget.update(context, glanceId)
                }
            } catch (e: Exception) {
                android.util.Log.w("WidgetUpdateManager", "Failed to refresh all quick dial widgets", e)
            }
        }
    }
}
