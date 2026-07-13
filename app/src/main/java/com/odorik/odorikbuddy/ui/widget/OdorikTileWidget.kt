package com.odorik.odorikbuddy.ui.widget

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.domain.usecase.ContactNameResolver
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class OdorikTileWidget : GlanceAppWidget() {

    companion object {
        // Per-widget color override keys (hybrid model: default to tile colors, allow override)
        val useTileColorsKey = booleanPreferencesKey("use_tile_colors")
        val widgetColorKey = longPreferencesKey("widget_color")
        val widgetTextColorKey = longPreferencesKey("widget_text_color")
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun tileRepository(): TileRepository
        fun contactNameResolver(): ContactNameResolver
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val tileId = prefs[intPreferencesKey("tile_id")] ?: -1
            val widgetStyle = prefs[stringPreferencesKey("widget_style")] ?: "SQUARE"

            // Hybrid coloring: per-widget overrides or fall back to tile
            val useTileColors = prefs[useTileColorsKey] ?: true
            val widgetColorOverride = prefs[widgetColorKey]
            val widgetTextColorOverride = prefs[widgetTextColorKey]

            val entryPoint = remember(context) {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
            }

            // Load tile asynchronously using produceState
            val tileState = produceState<TileEntity?>(initialValue = null, tileId) {
                if (tileId != -1) {
                    value = withTimeoutOrNull(1500) {
                        withContext(Dispatchers.IO) {
                            entryPoint.tileRepository().getTileById(tileId)
                        }
                    }
                } else {
                    value = null
                }
            }
            val tile = tileState.value

            // Load contact name asynchronously using produceState
            val contactNameState = produceState<String>(initialValue = "", tile) {
                if (tile != null) {
                    value = resolveContactName(context, entryPoint.contactNameResolver(), tile.recipient)
                } else {
                    value = ""
                }
            }
            val contactName = contactNameState.value

            WidgetContent(
                tile = tile,
                contactName = contactName,
                widgetStyle = widgetStyle,
                useTileColors = useTileColors,
                widgetColorOverride = widgetColorOverride,
                widgetTextColorOverride = widgetTextColorOverride
            )
        }
    }

    /**
     * Improved contact name resolution:
     * 1. First try the app's cached ContactNameResolver (fast, already loaded by the main app)
     * 2. Fall back to direct query only if needed (with simple in-memory cache)
     */
    private val contactNameCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    private suspend fun resolveContactName(
        context: Context,
        resolver: ContactNameResolver,
        phoneNumber: String
    ): String {
        // 1. Try the global cache first (best case - app already loaded contacts)
        val cached = resolver.getContactName(phoneNumber)
        if (cached != phoneNumber) return cached

        // 2. Check our lightweight widget cache
        contactNameCache[phoneNumber]?.let { return it }

        // 3. Last resort: direct query (only when necessary)
        val name = withContext(Dispatchers.IO) {
            queryContactNameDirectly(context, phoneNumber)
        }

        val finalName = name ?: phoneNumber
        contactNameCache[phoneNumber] = finalName
        return finalName
    }

    private fun queryContactNameDirectly(context: Context, phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                } else null
            }
        } catch (e: Exception) {
            android.util.Log.e("OdorikTileWidget", "Contact lookup failed for widget", e)
            null
        }
    }

    @Composable
    fun WidgetContent(
        tile: TileEntity?,
        contactName: String,
        widgetStyle: String,
        useTileColors: Boolean,
        widgetColorOverride: Long?,
        widgetTextColorOverride: Long?
    ) {
        val context = LocalContext.current
        val isDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES

        // Hybrid color resolution (per-widget override takes precedence)
        val effectiveBackgroundColor: Color = when {
            !useTileColors && widgetColorOverride != null -> Color(widgetColorOverride)
            tile?.color != null -> Color(tile.color)
            else -> if (isDark) Color(0xFF263238) else Color(0xFFECEFF1) // Dark / Light grey fallback for visibility
        }

        val effectiveTextColor: Color = when {
            !useTileColors && widgetTextColorOverride != null -> Color(widgetTextColorOverride)
            tile?.textColor != null -> Color(tile.textColor)
            tile?.color != null -> Color.White // Reasonable default for custom colored tiles
            else -> if (isDark) Color.White else Color.Black
        }

        if (tile == null) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(effectiveBackgroundColor)
                    .appWidgetBackground()
                    .cornerRadius(16.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                TextWithShadow(
                    text = "Select Tile",
                    style = TextStyle(color = androidx.glance.unit.ColorProvider(Color.White))
                )
            }
        } else if (widgetStyle == "CIRCLE") {
            // CIRCLE Style (Avatar)
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .applyTileClickAction(tile),
                contentAlignment = Alignment.Center
            ) {
                // Outer transparent box to allow for system rounding, 
                // but the content is a centered circle
                Box(
                    modifier = GlanceModifier
                        .size(64.dp)
                        .background(effectiveBackgroundColor)
                        .cornerRadius(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = getInitials(tile.label.ifEmpty { contactName })
                    TextWithShadow(
                        text = initials,
                        style = TextStyle(
                            color = androidx.glance.unit.ColorProvider(effectiveTextColor),
                            fontSize = 24.sp,
                            fontWeight = androidx.glance.text.FontWeight.Bold,
                            textAlign = androidx.glance.text.TextAlign.Center
                        )
                    )
                }
            }
        } else {
            // SQUARE Style (Classic)
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(effectiveBackgroundColor)
                    .appWidgetBackground()
                    .cornerRadius(16.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .applyTileClickAction(tile)
                ) {
                    val titleText = if (tile.label.isNotEmpty()) tile.label else contactName
                    val subtitleText = if (tile.label.isNotEmpty()) contactName else if (contactName != tile.recipient) tile.recipient else ""
                    
                    TextWithShadow(
                        text = titleText,
                        style = TextStyle(
                            color = androidx.glance.unit.ColorProvider(effectiveTextColor),
                            fontSize = 12.sp,
                            fontWeight = androidx.glance.text.FontWeight.Bold,
                            textAlign = androidx.glance.text.TextAlign.Center
                        ),
                        maxLines = 1
                    )
                    
                    if (subtitleText.isNotEmpty()) {
                        TextWithShadow(
                            text = subtitleText,
                            style = TextStyle(
                                color = androidx.glance.unit.ColorProvider(effectiveTextColor.copy(alpha = 0.8f)),
                                fontSize = 10.sp,
                                textAlign = androidx.glance.text.TextAlign.Center
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    private fun getInitials(name: String): String {
        if (name.isEmpty()) return "?"
        val parts = name.trim().split(Regex("\\s+"))
        return if (parts.size >= 2) {
            (parts[0].take(1) + parts[1].take(1)).uppercase()
        } else {
            name.take(1).uppercase()
        }
    }

    @Composable
    private fun TextWithShadow(
        text: String,
        style: TextStyle,
        maxLines: Int = 1
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Shadow Layer - Increased opacity and offset for better readability
            Text(
                text = text,
                style = style.copy(
                    color = androidx.glance.unit.ColorProvider(Color.Black.copy(alpha = 0.8f))
                ),
                maxLines = maxLines,
                modifier = GlanceModifier.padding(start = 1.dp, top = 1.dp)
            )
            // Main Text Layer
            Text(
                text = text,
                style = style,
                maxLines = maxLines
            )
        }
    }

    private fun GlanceModifier.applyTileClickAction(tile: TileEntity): GlanceModifier {
        // Always use WidgetCallActivity for consistency and reliability
        return this.clickable(
            actionStartActivity<WidgetCallActivity>(
                actionParametersOf(ActionParameters.Key<Int>("tile_id") to tile.id)
            )
        )
    }
}