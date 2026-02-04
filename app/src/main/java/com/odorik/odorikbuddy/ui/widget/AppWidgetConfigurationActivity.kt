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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.ui.theme.OdorikBuddyTheme
import com.odorik.odorikbuddy.ui.theme.SettingsAccent
import com.odorik.odorikbuddy.ui.theme.SettingsAccentLight
import com.odorik.odorikbuddy.util.getResponsiveBodyLargeSize
import com.odorik.odorikbuddy.util.getResponsiveCardPadding
import com.odorik.odorikbuddy.util.getResponsiveSpacing
import com.odorik.odorikbuddy.util.getResponsiveTitleLargeSize
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
                    topBar = {
                        GradientHeader(
                            title = stringResource(R.string.widget_config_title),
                            onBackClick = { finish() }
                        )
                    }
                ) { padding ->
                    TileSelectionScreen(
                        modifier = Modifier.padding(padding),
                        viewModel = viewModel,
                        onTileSelected = { tileId, style ->
                            saveWidgetState(tileId, style)
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun GradientHeader(
        title: String,
        onBackClick: () -> Unit
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                SettingsAccent.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(SettingsAccent, SettingsAccentLight)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = title,
                        fontSize = getResponsiveTitleLargeSize(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
    
    @Composable
    fun TileSelectionScreen(
        modifier: Modifier = Modifier,
        viewModel: AppWidgetConfigurationViewModel,
        onTileSelected: (Int, String) -> Unit
    ) {
        val tiles by viewModel.tiles.collectAsState()
        val contactsMap by viewModel.contactsMap.collectAsState()
        var selectedStyle by remember { mutableStateOf("SQUARE") }
        
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
                            
                            StyleOption(
                                label = stringResource(R.string.style_square),
                                isSelected = selectedStyle == "SQUARE",
                                onClick = { selectedStyle = "SQUARE" },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            
                            
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

                items(tiles, key = { it.id }) { tile ->
                    val contactName = viewModel.getContactName(tile.recipient)
                    
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = getResponsiveCardPadding(), vertical = getResponsiveSpacing() / 2)
                            .clickable { onTileSelected(tile.id, selectedStyle) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (tile.label.isNotEmpty()) {
                                Text(
                                    text = tile.label,
                                    fontSize = getResponsiveBodyLargeSize(),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = contactName,
                                    fontSize = getResponsiveBodyLargeSize() * 0.9f,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = contactName,
                                    fontSize = getResponsiveBodyLargeSize(),
                                    fontWeight = FontWeight.Bold
                                )
                                if (contactName != tile.recipient) {
                                    Text(
                                        text = tile.recipient,
                                        fontSize = getResponsiveBodyLargeSize() * 0.9f,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Text(
                                text = if (tile.callType == "CALLBACK") stringResource(R.string.call_type_callback) else stringResource(R.string.call_type_oneshot),
                                fontSize = getResponsiveBodyLargeSize() * 0.8f,
                                color = SettingsAccent,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
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

    private fun saveWidgetState(tileId: Int, style: String) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(appWidgetId)
            
            updateAppWidgetState(applicationContext, glanceId) { prefs ->
                prefs[intPreferencesKey("tile_id")] = tileId
                prefs[stringPreferencesKey("widget_style")] = style
            }
            
            OdorikTileWidget().update(applicationContext, glanceId)
            
            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}
