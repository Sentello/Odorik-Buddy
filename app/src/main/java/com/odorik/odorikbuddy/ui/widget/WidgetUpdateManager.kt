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


@Singleton
class WidgetUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO)


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
