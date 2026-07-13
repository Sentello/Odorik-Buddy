package com.odorik.odorikbuddy.ui.widget

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.ui.calls.TileColorHelper
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.components.darkModeBorder
import com.odorik.odorikbuddy.ui.theme.OdorikBuddyTheme
import com.odorik.odorikbuddy.ui.theme.SettingsAccent
import com.odorik.odorikbuddy.ui.theme.SettingsAccentLight
import com.odorik.odorikbuddy.util.getResponsiveBodyLargeSize
import com.odorik.odorikbuddy.util.getResponsiveCardPadding
import com.odorik.odorikbuddy.util.getResponsiveSpacing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppWidgetConfigurationActivity : ComponentActivity() {

    @Inject
    lateinit var tileRepository: TileRepository

    @Inject
    lateinit var themeManager: ThemeManager

    private val viewModel: AppWidgetConfigurationViewModel by viewModels()

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Get the App Widget ID from the Intent
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        // Return cancelled by default
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        setContent {
            val context = LocalContext.current
            
            val readContactsPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    if (isGranted) {
                        viewModel.loadContacts(context.contentResolver)
                    }
                }
            )

            LaunchedEffect(Unit) {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) -> {
                        viewModel.loadContacts(context.contentResolver)
                    }
                    else -> {
                        readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }
            }

            OdorikBuddyTheme(themeManager = themeManager) {
                Scaffold(
                    contentWindowInsets = WindowInsets(0.dp),
                    topBar = {
                        GradientHeader(
                            title = stringResource(R.string.widget_config_title),
                            iconVector = Icons.Default.Widgets,
                            backgroundBrush = Brush.verticalGradient(
                                colors = listOf(SettingsAccent.copy(alpha = 0.15f), Color.Transparent)
                            ),
                            iconGradientBrush = Brush.linearGradient(
                                colors = listOf(SettingsAccent, SettingsAccentLight)
                            ),
                            onBackClick = { finish() }
                        )
                    }
                ) { padding ->
                    TileSelectionScreen(
                        modifier = Modifier.padding(padding),
                        viewModel = viewModel,
                        onSaveWidget = { tileId, style, useTileColors, customColor, customTextColor ->
                            saveWidgetState(tileId, style, useTileColors, customColor, customTextColor)
                        }
                    )
                }
            }
        }
    }


    @Composable
    fun TileSelectionScreen(
        modifier: Modifier = Modifier,
        viewModel: AppWidgetConfigurationViewModel,
        onSaveWidget: (tileId: Int, style: String, useTileColors: Boolean, customColor: Long?, customTextColor: Long?) -> Unit
    ) {
        val tiles by viewModel.tiles.collectAsState()
        val contactsMap by viewModel.contactsMap.collectAsState()

        var selectedStyle by remember { mutableStateOf("SQUARE") }
        var selectedTileId by remember { mutableStateOf<Int?>(null) }
        var useTileColors by remember { mutableStateOf(true) }
        var customColor by remember { mutableStateOf<Long?>(null) }
        var customTextColor by remember { mutableStateOf<Long?>(null) }

        val selectedTile = tiles.find { it.id == selectedTileId }

        androidx.compose.runtime.key(contactsMap) {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = getResponsiveSpacing())
            ) {
                item {
                    Text(
                        text = stringResource(R.string.widget_config_instruction),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            horizontal = getResponsiveCardPadding(),
                            vertical = getResponsiveSpacing()
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Style Selector
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = getResponsiveCardPadding())
                            .padding(bottom = getResponsiveSpacing())
                    ) {
                        Text(
                            text = stringResource(R.string.widget_style),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Square Option
                            StyleOption(
                                label = stringResource(R.string.style_square),
                                isSelected = selectedStyle == "SQUARE",
                                onClick = { selectedStyle = "SQUARE" },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Circle Option
                            StyleOption(
                                label = stringResource(R.string.style_circle),
                                isSelected = selectedStyle == "CIRCLE",
                                onClick = { selectedStyle = "CIRCLE" },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Live Preview (greatly improved in this version)
                if (selectedTile != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = getResponsiveCardPadding())
                                .padding(vertical = getResponsiveSpacing() / 2)
                        ) {
                            Text(
                                text = stringResource(R.string.widget_live_preview),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            QuickDialWidgetPreview(
                                tile = selectedTile,
                                contactName = viewModel.getContactName(selectedTile.recipient),
                                style = selectedStyle,
                                useTileColors = useTileColors,
                                customColor = customColor,
                                customTextColor = customTextColor
                            )
                        }
                    }
                }

                // Hybrid coloring section - only shown after user selects a tile
                if (selectedTileId != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = getResponsiveCardPadding())
                                .padding(top = getResponsiveSpacing(), bottom = getResponsiveSpacing() / 2)
                        ) {
                            Text(
                                text = stringResource(R.string.widget_colors),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { useTileColors = !useTileColors }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Checkbox(
                                    checked = useTileColors,
                                    onCheckedChange = { useTileColors = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.widget_use_tile_colors),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            if (!useTileColors && selectedTile != null) {
                                Text(
                                    text = stringResource(R.string.widget_custom_colors),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )

                                // Background color swatches (reuse palette)
                                Text(stringResource(R.string.color), style = MaterialTheme.typography.labelSmall)
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    item {
                                        ColorSwatch(
                                            color = null,
                                            isSelected = customColor == null,
                                            onClick = { customColor = null }
                                        )
                                    }
                                    items(TileColorHelper.allBaseColors) { baseColor ->
                                        val display = TileColorHelper.resolveColor(baseColor, isSystemInDarkTheme())
                                            ?: Color(baseColor)
                                        ColorSwatch(
                                            color = display,
                                            isSelected = customColor == baseColor,
                                            onClick = { customColor = baseColor }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Text color swatches
                                Text(stringResource(R.string.text_color), style = MaterialTheme.typography.labelSmall)
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    item {
                                        ColorSwatch(
                                            color = null,
                                            isSelected = customTextColor == null,
                                            isTextSwatch = true,
                                            onClick = { customTextColor = null }
                                        )
                                    }
                                    items(TileColorHelper.textColors) { c ->
                                        ColorSwatch(
                                            color = Color(c),
                                            isSelected = customTextColor == c,
                                            isTextSwatch = true,
                                            onClick = { customTextColor = c }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(tiles, key = { it.id }) { tile ->
                    val contactName = viewModel.getContactName(tile.recipient)
                    val isSelected = selectedTileId == tile.id
                    
                    // Show actual tile colors (big improvement)
                    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
                    val tileBg = TileColorHelper.resolveColor(tile.color, isSystemDark)
                        ?: MaterialTheme.colorScheme.surface
                    val tileText = if (tile.textColor != null) {
                        Color(tile.textColor)
                    } else if (tile.color != null) {
                        if (isSystemDark) Color.White else Color.Black
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = getResponsiveCardPadding(), vertical = getResponsiveSpacing() / 2)
                            .darkModeBorder(RoundedCornerShape(16.dp))
                            .clickable {
                                selectedTileId = tile.id
                                // Reset custom colors when changing tile
                                if (!useTileColors) {
                                    customColor = tile.color
                                    customTextColor = tile.textColor
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = tileBg
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (tile.label.isNotEmpty()) {
                                Text(
                                    text = tile.label,
                                    fontSize = getResponsiveBodyLargeSize(),
                                    fontWeight = FontWeight.Bold,
                                    color = tileText
                                )
                                Text(
                                    text = contactName,
                                    fontSize = getResponsiveBodyLargeSize() * 0.9f,
                                    color = tileText.copy(alpha = 0.75f)
                                )
                            } else {
                                Text(
                                    text = contactName,
                                    fontSize = getResponsiveBodyLargeSize(),
                                    fontWeight = FontWeight.Bold,
                                    color = tileText
                                )
                                if (contactName != tile.recipient) {
                                    Text(
                                        text = tile.recipient,
                                        fontSize = getResponsiveBodyLargeSize() * 0.9f,
                                        color = tileText.copy(alpha = 0.75f)
                                    )
                                }
                            }
                            
                            Text(
                                text = if (tile.callType == "CALLBACK") stringResource(R.string.call_type_callback) else stringResource(R.string.call_type_oneshot),
                                fontSize = getResponsiveBodyLargeSize() * 0.8f,
                                color = if (tile.color != null) tileText.copy(alpha = 0.85f) else SettingsAccent,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            
                            if (isSelected) {
                                Text(
                                    text = "✓ Selected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tileText.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Save button - always at bottom, respects navigation insets
                item {
                    val canSave = selectedTileId != null
                    Button(
                        onClick = {
                            selectedTileId?.let { tid ->
                                onSaveWidget(
                                    tid,
                                    selectedStyle,
                                    useTileColors,
                                    if (!useTileColors) customColor else null,
                                    if (!useTileColors) customTextColor else null
                                )
                            }
                        },
                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = getResponsiveCardPadding(), vertical = 16.dp)
                            .navigationBarsPadding()
                    ) {
                        Text(
                            text = if (canSave) stringResource(R.string.save_widget) else stringResource(R.string.select_tile_first)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun StyleOption(
        label: String,
        isSelected: Boolean,
        onClick: () -> Unit,
        shape: androidx.compose.ui.graphics.Shape,
        modifier: Modifier = Modifier
    ) {
        val primaryColor = SettingsAccent
        val outlineColor = MaterialTheme.colorScheme.outlineVariant
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        
        Column(
            modifier = modifier
                .clickable(onClick = onClick)
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Shape Preview
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) primaryColor else outlineColor,
                        shape = shape
                    )
                    .clip(shape)
                    .background(if (isSelected) primaryColor.copy(alpha = 0.1f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                // Inner dummy content
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isSelected) primaryColor.copy(alpha = 0.3f) 
                            else outlineColor.copy(alpha = 0.3f), 
                            shape
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Label
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) primaryColor else onSurfaceColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun ColorSwatch(
        color: Color?,
        isSelected: Boolean,
        isTextSwatch: Boolean = false,
        onClick: () -> Unit
    ) {
        val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        val bg = color ?: MaterialTheme.colorScheme.surfaceVariant

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(bg)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (color == null) {
                Text(
                    text = if (isTextSwatch) "A" else "—",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    /**
     * High quality preview that closely mimics the real Glance widget.
     * This is a big improvement over the previous minimal style selector.
     */
    @Composable
    fun QuickDialWidgetPreview(
        tile: com.odorik.odorikbuddy.data.local.entity.TileEntity,
        contactName: String,
        style: String,
        useTileColors: Boolean,
        customColor: Long?,
        customTextColor: Long?
    ) {
        val isSystemDark = isSystemInDarkTheme()

        // Same resolution logic as the real widget + TileColorHelper
        val bgColor = when {
            !useTileColors && customColor != null -> Color(customColor)
            tile.color != null -> TileColorHelper.resolveColor(tile.color, isSystemDark)
                ?: Color(tile.color)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

        val textCol = when {
            !useTileColors && customTextColor != null -> Color(customTextColor)
            tile.textColor != null -> Color(tile.textColor)
            tile.color != null -> if (isSystemDark) Color.White else Color.Black
            else -> MaterialTheme.colorScheme.onSurface
        }

        val titleText = if (tile.label.isNotEmpty()) tile.label else contactName
        val subtitleText = if (tile.label.isNotEmpty()) contactName else if (contactName != tile.recipient) tile.recipient else ""

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (style == "CIRCLE") {
                // CIRCLE style preview
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(textCol.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = (tile.label.ifEmpty { contactName })
                        .trim()
                        .split(Regex("\\s+"))
                        .let { parts ->
                            if (parts.size >= 2) (parts[0].take(1) + parts[1].take(1)).uppercase()
                            else parts.firstOrNull()?.take(1)?.uppercase() ?: "?"
                        }

                    Text(
                        text = initials,
                        color = textCol,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // SQUARE style preview
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = titleText,
                        color = textCol,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitleText.isNotEmpty()) {
                        Text(
                            text = subtitleText,
                            color = textCol.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }

    private fun saveWidgetState(
        tileId: Int,
        style: String,
        useTileColors: Boolean,
        customColor: Long?,
        customTextColor: Long?
    ) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(appWidgetId)

            updateAppWidgetState(applicationContext, glanceId) { prefs ->
                prefs[intPreferencesKey("tile_id")] = tileId
                prefs[stringPreferencesKey("widget_style")] = style
                prefs[OdorikTileWidget.useTileColorsKey] = useTileColors
                if (!useTileColors) {
                    if (customColor != null) prefs[OdorikTileWidget.widgetColorKey] = customColor
                    if (customTextColor != null) prefs[OdorikTileWidget.widgetTextColorKey] = customTextColor
                } else {
                    prefs.remove(OdorikTileWidget.widgetColorKey)
                    prefs.remove(OdorikTileWidget.widgetTextColorKey)
                }
            }

            OdorikTileWidget().update(applicationContext, glanceId)

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}
