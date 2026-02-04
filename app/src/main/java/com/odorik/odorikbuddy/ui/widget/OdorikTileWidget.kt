package com.odorik.odorikbuddy.ui.widget

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
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
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.data.repository.TileRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking

class OdorikTileWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun tileRepository(): TileRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        
        
        provideContent {
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val tileId = prefs[intPreferencesKey("tile_id")] ?: -1
            val widgetStyle = prefs[stringPreferencesKey("widget_style")] ?: "SQUARE"
            
            var tile: TileEntity? = null
            var contactName: String = ""

            if (tileId != -1) {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                tile = runBlocking { entryPoint.tileRepository().getTileById(tileId) }
                if (tile != null) {
                    contactName = getContactName(context, tile.recipient)
                }
            }

            WidgetContent(tile, contactName, widgetStyle)
        }
    }

    private fun getContactName(context: Context, phoneNumber: String): String {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var contactName = ""
        
        try {
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    contactName = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                }
            }
        } catch (e: Exception) {
            
             e.printStackTrace()
        }
        
        return if (contactName.isNotEmpty()) contactName else phoneNumber
    }

    @Composable
    fun WidgetContent(tile: TileEntity?, contactName: String, widgetStyle: String) {
        val backgroundColor = if (tile?.color != null) {
            Color(tile.color)
        } else {
            Color.Transparent
        }
        
        val textColor = if (tile?.textColor != null) {
            Color(tile.textColor)
        } else if (tile?.color != null) {
            Color.White 
        } else {
            androidx.glance.unit.ColorProvider(R.color.black).getColor(LocalContext.current)
        }

        if (tile == null) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(backgroundColor)
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
                        .background(backgroundColor)
                        .cornerRadius(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = getInitials(tile.label.ifEmpty { contactName })
                    TextWithShadow(
                        text = initials,
                        style = TextStyle(
                            color = androidx.glance.unit.ColorProvider(textColor),
                            fontSize = 24.sp,
                            fontWeight = androidx.glance.text.FontWeight.Bold,
                            textAlign = androidx.glance.text.TextAlign.Center
                        )
                    )
                }
            }
        } else {
            
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(backgroundColor)
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
                            color = androidx.glance.unit.ColorProvider(textColor),
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
                                color = androidx.glance.unit.ColorProvider(textColor.copy(alpha = 0.8f)),
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
            
            Text(
                text = text,
                style = style.copy(
                    color = androidx.glance.unit.ColorProvider(Color.Black.copy(alpha = 0.8f))
                ),
                maxLines = maxLines,
                modifier = GlanceModifier.padding(start = 1.dp, top = 1.dp)
            )
            
            Text(
                text = text,
                style = style,
                maxLines = maxLines
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