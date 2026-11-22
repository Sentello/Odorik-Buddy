package com.odorik.odorikbuddy.ui.widget

import android.content.Context
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LanguagePreferences
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.ui.calls.TileColorHelper
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.OdorikBuddyTheme
import com.odorik.odorikbuddy.ui.theme.ScreenAccents
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TileWidgetConfig(
    val tileId: Int?,
    val style: TileWidgetStyle,
    val useTileColors: Boolean,
    val customColor: Long?,
    val customTextColor: Long?,
    val textSize: TileWidgetTextSize
) {
    companion object {
        val DEFAULT = TileWidgetConfig(
            tileId = null,
            style = TileWidgetStyle.SQUARE,
            useTileColors = true,
            customColor = null,
            customTextColor = null,
            textSize = TileWidgetTextSize.NORMAL
        )
    }
}

@AndroidEntryPoint
class AppWidgetConfigurationActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguagePreferences.wrapContext(newBase))
    }

    @Inject
    lateinit var tileRepository: TileRepository

    @Inject
    lateinit var themeManager: ThemeManager

    private val viewModel: AppWidgetConfigurationViewModel by viewModels()

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private var initialConfig by mutableStateOf<TileWidgetConfig?>(null)

    private var isSaving by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        lifecycleScope.launch {
            initialConfig = try {
                val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(appWidgetId)
                val prefs = getAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, glanceId)
                TileWidgetConfig(
                    tileId = prefs[OdorikTileWidget.tileIdKey]?.takeIf { it != OdorikTileWidget.NO_TILE },
                    style = TileWidgetStyle.fromKey(prefs[OdorikTileWidget.widgetStyleKey]),
                    useTileColors = prefs[OdorikTileWidget.useTileColorsKey] ?: true,
                    customColor = prefs[OdorikTileWidget.widgetColorKey],
                    customTextColor = prefs[OdorikTileWidget.widgetTextColorKey],
                    textSize = TileWidgetTextSize.fromKey(prefs[OdorikTileWidget.textSizeKey])
                )
            } catch (e: Exception) {
                android.util.Log.w("AppWidgetConfig", "Could not read widget state, using defaults", e)
                TileWidgetConfig.DEFAULT
            }
        }

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
                            accent = ScreenAccents.Settings,
                            onBackClick = { finish() }
                        )
                    }
                ) { padding ->
                    val initial = initialConfig
                    if (initial == null) {
                        Box(
                            modifier = Modifier.padding(padding).fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        TileSelectionScreen(
                            modifier = Modifier.padding(padding),
                            viewModel = viewModel,
                            initial = initial,
                            onSaveWidget = { tileId, style, useTileColors, customColor, customTextColor, textSize ->
                                saveWidgetState(tileId, style, useTileColors, customColor, customTextColor, textSize)
                            }
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun TileSelectionScreen(
        modifier: Modifier = Modifier,
        viewModel: AppWidgetConfigurationViewModel,
        initial: TileWidgetConfig,
        onSaveWidget: (tileId: Int, style: TileWidgetStyle, useTileColors: Boolean, customColor: Long?, customTextColor: Long?, textSize: TileWidgetTextSize) -> Unit
    ) {
        val tiles by viewModel.tiles.collectAsState()
        val contactsMap by viewModel.contactsMap.collectAsState()

        var selectedStyle by rememberSaveable { mutableStateOf(initial.style) }
        var selectedTileId by rememberSaveable { mutableStateOf(initial.tileId) }
        var useTileColors by rememberSaveable { mutableStateOf(initial.useTileColors) }
        var customColor by rememberSaveable { mutableStateOf(initial.customColor) }
        var customTextColor by rememberSaveable { mutableStateOf(initial.customTextColor) }
        var selectedTextSize by rememberSaveable { mutableStateOf(initial.textSize) }

        val selectedTile = tiles.find { it.id == selectedTileId }

        androidx.compose.runtime.key(contactsMap) {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalAppDimens.current.spacing)
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = LocalAppDimens.current.cardPadding,
                            vertical = LocalAppDimens.current.spacing
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.widget_config_instruction),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.widget_config_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LocalAppDimens.current.cardPadding)
                            .padding(bottom = LocalAppDimens.current.spacing)
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
                            StyleOption(
                                label = stringResource(R.string.style_square),
                                isSelected = selectedStyle == TileWidgetStyle.SQUARE,
                                onClick = { selectedStyle = TileWidgetStyle.SQUARE },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            
                            StyleOption(
                                label = stringResource(R.string.style_circle),
                                isSelected = selectedStyle == TileWidgetStyle.CIRCLE,
                                onClick = { selectedStyle = TileWidgetStyle.CIRCLE },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LocalAppDimens.current.cardPadding)
                            .padding(bottom = LocalAppDimens.current.spacing)
                    ) {
                        Text(
                            text = stringResource(R.string.widget_config_text_size),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        val sizes = listOf(
                            TileWidgetTextSize.NORMAL to stringResource(R.string.widget_config_normal),
                            TileWidgetTextSize.LARGE to stringResource(R.string.widget_config_large),
                            TileWidgetTextSize.EXTRA_LARGE to stringResource(R.string.widget_config_extra_large)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            sizes.forEach { (size, label) ->
                                FilterChip(
                                    selected = selectedTextSize == size,
                                    onClick = { selectedTextSize = size },
                                    label = { Text(label) },
                                    leadingIcon = if (selectedTextSize == size) {
                                        { Icon(Icons.Default.Check, null) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                if (selectedTile != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = LocalAppDimens.current.cardPadding)
                                .padding(vertical = LocalAppDimens.current.spacing / 2)
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
                                textSize = selectedTextSize,
                                useTileColors = useTileColors,
                                customColor = customColor,
                                customTextColor = customTextColor
                            )
                        }
                    }
                }

                if (selectedTileId != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = LocalAppDimens.current.cardPadding)
                                .padding(top = LocalAppDimens.current.spacing, bottom = LocalAppDimens.current.spacing / 2)
                        ) {
                            Text(
                                text = stringResource(R.string.widget_colors),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

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
                                        val display = TileColorHelper.resolveColor(
                                            baseColor,
                                            isSystemInDarkTheme()
                                        )
                                            ?: Color(baseColor)
                                        ColorSwatch(
                                            color = display,
                                            isSelected = customColor == baseColor,
                                            onClick = { customColor = baseColor }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

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
                    
                    val isSystemDark = com.odorik.odorikbuddy.ui.theme.LocalIsAppDark.current
                    val tileBg = TileColorHelper.resolveColor(tile.color, isSystemDark)
                        ?: MaterialTheme.colorScheme.surface
                    val explicitText = TileColorHelper.readableTextColor(tile.textColor, tileBg)
                    val tileText = if (explicitText != null) {
                        explicitText
                    } else if (tile.color != null) {
                        if (isSystemDark) Color.White else Color.Black
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LocalAppDimens.current.cardPadding, vertical = LocalAppDimens.current.spacing / 2)
                            .clickable {
                                val tileChanged = selectedTileId != tile.id
                                selectedTileId = tile.id
                                if (tileChanged && !useTileColors) {
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = tileText
                                )
                                Text(
                                    text = contactName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tileText.copy(alpha = 0.75f)
                                )
                            } else {
                                Text(
                                    text = contactName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = tileText
                                )
                                if (contactName != tile.recipient) {
                                    Text(
                                        text = tile.recipient,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = tileText.copy(alpha = 0.75f)
                                    )
                                }
                            }
                            
                            Text(
                                text = if (tile.callType == com.odorik.odorikbuddy.data.local.entity.TileCallType.CALLBACK) stringResource(R.string.call_type_callback) else stringResource(R.string.call_type_oneshot),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (tile.color != null) tileText.copy(alpha = 0.85f) else ScreenAccents.Settings.main(),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            
                            if (isSelected) {
                                Text(
                                    text = stringResource(R.string.widget_tile_selected),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tileText.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    val canSave = selectedTile != null && !isSaving
                    Button(
                        onClick = {
                            selectedTileId?.let { tid ->
                                onSaveWidget(
                                    tid,
                                    selectedStyle,
                                    useTileColors,
                                    if (!useTileColors) customColor else null,
                                    if (!useTileColors) customTextColor else null,
                                    selectedTextSize
                                )
                            }
                        },
                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LocalAppDimens.current.cardPadding, vertical = 16.dp)
                            .navigationBarsPadding()
                    ) {
                        Text(
                            text = if (selectedTile != null) stringResource(R.string.save_widget) else stringResource(R.string.select_tile_first)
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
        val primaryColor = ScreenAccents.Settings.main()
        val outlineColor = MaterialTheme.colorScheme.outlineVariant
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        
        Column(
            modifier = modifier
                .clickable(onClick = onClick)
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

    @Composable
    fun QuickDialWidgetPreview(
        tile: com.odorik.odorikbuddy.data.local.entity.TileEntity,
        contactName: String,
        style: TileWidgetStyle,
        textSize: TileWidgetTextSize,
        useTileColors: Boolean,
        customColor: Long?,
        customTextColor: Long?
    ) {
        val isPhoneDark = isSystemInDarkTheme()

        val baseColor = if (!useTileColors && customColor != null) customColor else tile.color
        val textColor = if (!useTileColors && customTextColor != null) customTextColor else tile.textColor
        val colors = TileColorHelper.widgetColors(baseColor, textColor, isPhoneDark)
        val bgColor = colors.background
        val textCol = colors.text

        val shadow = if (colors.shadow == Color.Transparent) null else with(LocalDensity.current) {
            Shadow(color = colors.shadow, offset = Offset(1.dp.toPx(), 1.dp.toPx()))
        }
        val textStyle = LocalTextStyle.current.copy(shadow = shadow)

        val titleText = TileWidgetTexts.title(tile.label, contactName)
        val subtitleText = TileWidgetTexts.subtitle(tile.label, contactName, tile.recipient)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 92.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (style == TileWidgetStyle.CIRCLE) MaterialTheme.colorScheme.surfaceContainer else bgColor)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (style == TileWidgetStyle.CIRCLE) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(bgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = TileWidgetTexts.initials(titleText)

                    Text(
                        text = initials,
                        style = textStyle,
                        color = textCol,
                        fontSize = textSize.initialsSp.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = titleText,
                        style = textStyle,
                        color = textCol,
                        fontSize = textSize.titleSp.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = textSize.titleMaxLines,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitleText.isNotEmpty()) {
                        Text(
                            text = subtitleText,
                            style = textStyle,
                            color = textCol.copy(alpha = TileWidgetTexts.SUBTITLE_ALPHA),
                            fontSize = textSize.subtitleSp.sp,
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
        style: TileWidgetStyle,
        useTileColors: Boolean,
        customColor: Long?,
        customTextColor: Long?,
        textSize: TileWidgetTextSize
    ) {
        if (isSaving) return
        isSaving = true

        lifecycleScope.launch {
            try {
                val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(appWidgetId)

                updateAppWidgetState(applicationContext, glanceId) { prefs ->
                    prefs[OdorikTileWidget.tileIdKey] = tileId
                    prefs[OdorikTileWidget.widgetStyleKey] = style.name
                    prefs[OdorikTileWidget.useTileColorsKey] = useTileColors
                    prefs[OdorikTileWidget.textSizeKey] = textSize.name
                    if (!useTileColors && customColor != null) {
                        prefs[OdorikTileWidget.widgetColorKey] = customColor
                    } else {
                        prefs.remove(OdorikTileWidget.widgetColorKey)
                    }
                    if (!useTileColors && customTextColor != null) {
                        prefs[OdorikTileWidget.widgetTextColorKey] = customTextColor
                    } else {
                        prefs.remove(OdorikTileWidget.widgetTextColorKey)
                    }
                }

                OdorikTileWidget().update(applicationContext, glanceId)

                val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.w("AppWidgetConfig", "Could not save widget state", e)
            }
            finish()
        }
    }
}
