package com.odorik.odorikbuddy.ui.widget

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
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
import com.odorik.odorikbuddy.ui.calls.TileColorHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class OdorikTileWidget : GlanceAppWidget() {

    companion object {
        const val NO_TILE = -1

        val tileIdKey = intPreferencesKey("tile_id")
        val widgetStyleKey = stringPreferencesKey("widget_style")

        val useTileColorsKey = booleanPreferencesKey("use_tile_colors")
        val widgetColorKey = longPreferencesKey("widget_color")
        val widgetTextColorKey = longPreferencesKey("widget_text_color")
        val textSizeKey = stringPreferencesKey("text_size")
    }

    override val sizeMode: SizeMode = SizeMode.Exact

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun tileRepository(): TileRepository
        fun contactNameResolver(): ContactNameResolver
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val tileId = prefs[tileIdKey] ?: NO_TILE
            val widgetStyle = TileWidgetStyle.fromKey(prefs[widgetStyleKey])
            val textSize = TileWidgetTextSize.fromKey(prefs[textSizeKey])

            val useTileColors = prefs[useTileColorsKey] ?: true
            val widgetColorOverride = prefs[widgetColorKey]
            val widgetTextColorOverride = prefs[widgetTextColorKey]

            val entryPoint = remember(context) {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
            }

            val tileState = produceState<TileEntity?>(initialValue = null, tileId) {
                if (tileId != NO_TILE) {
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
                textSize = textSize,
                useTileColors = useTileColors,
                widgetColorOverride = widgetColorOverride,
                widgetTextColorOverride = widgetTextColorOverride
            )
        }
    }

    private val contactNameCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    private suspend fun resolveContactName(
        context: Context,
        resolver: ContactNameResolver,
        phoneNumber: String
    ): String {
        val cached = resolver.getContactName(phoneNumber)
        if (cached != phoneNumber) return cached

        contactNameCache[phoneNumber]?.let { return it }

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
        widgetStyle: TileWidgetStyle,
        textSize: TileWidgetTextSize,
        useTileColors: Boolean,
        widgetColorOverride: Long?,
        widgetTextColorOverride: Long?
    ) {
        val context = LocalContext.current

        val baseColor = if (!useTileColors && widgetColorOverride != null) widgetColorOverride else tile?.color
        val textColor = if (!useTileColors && widgetTextColorOverride != null) widgetTextColorOverride else tile?.textColor

        val day = TileColorHelper.widgetColors(baseColor, textColor, isDark = false)
        val night = TileColorHelper.widgetColors(baseColor, textColor, isDark = true)
        val effectiveBackgroundColor = ColorProvider(day = day.background, night = night.background)
        val effectiveTextColor = ColorProvider(day = day.text, night = night.text)
        val subtitleTextColor = ColorProvider(
            day = day.text.copy(alpha = TileWidgetTexts.SUBTITLE_ALPHA),
            night = night.text.copy(alpha = TileWidgetTexts.SUBTITLE_ALPHA)
        )
        val shadowColor = ColorProvider(day = day.shadow, night = night.shadow)

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
                    text = context.getString(com.odorik.odorikbuddy.R.string.widget_select_tile),
                    style = TextStyle(color = effectiveTextColor),
                    shadowColor = shadowColor
                )
            }
        } else if (widgetStyle == TileWidgetStyle.CIRCLE) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .applyTileClickAction(tile),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(64.dp)
                        .background(effectiveBackgroundColor)
                        .cornerRadius(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = TileWidgetTexts.initials(TileWidgetTexts.title(tile.label, contactName))
                    TextWithShadow(
                        text = initials,
                        style = TextStyle(
                            color = effectiveTextColor,
                            fontSize = textSize.initialsSp.sp,
                            fontWeight = androidx.glance.text.FontWeight.Bold,
                            textAlign = androidx.glance.text.TextAlign.Center
                        ),
                        shadowColor = shadowColor
                    )
                }
            }
        } else {
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
                    val titleText = TileWidgetTexts.title(tile.label, contactName)
                    val subtitleText = TileWidgetTexts.subtitle(tile.label, contactName, tile.recipient)
                    val showSubtitle = subtitleText.isNotEmpty() && textSize.subtitleFits(
                        availableHeightDp = LocalSize.current.height.value,
                        fontScale = context.resources.configuration.fontScale
                    )

                    TextWithShadow(
                        text = titleText,
                        style = TextStyle(
                            color = effectiveTextColor,
                            fontSize = textSize.titleSp.sp,
                            fontWeight = androidx.glance.text.FontWeight.Bold,
                            textAlign = androidx.glance.text.TextAlign.Center
                        ),
                        shadowColor = shadowColor,
                        maxLines = textSize.titleMaxLines
                    )
                    
                    if (showSubtitle) {
                        TextWithShadow(
                            text = subtitleText,
                            style = TextStyle(
                                color = subtitleTextColor,
                                fontSize = textSize.subtitleSp.sp,
                                textAlign = androidx.glance.text.TextAlign.Center
                            ),
                            shadowColor = shadowColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun TextWithShadow(
        text: String,
        style: TextStyle,
        shadowColor: androidx.glance.unit.ColorProvider,
        maxLines: Int = 1
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = style.copy(color = shadowColor),
                maxLines = maxLines,
                modifier = GlanceModifier.padding(start = 1.dp, top = 1.dp)
            )
            Text(
                text = text,
                style = style,
                maxLines = maxLines,
                modifier = GlanceModifier.padding(end = 1.dp, bottom = 1.dp)
            )
        }
    }

    private fun GlanceModifier.applyTileClickAction(tile: TileEntity): GlanceModifier {
        return this.clickable(
            actionStartActivity<WidgetCallActivity>(
                actionParametersOf(ActionParameters.Key<Int>("tile_id") to tile.id)
            )
        )
    }
}