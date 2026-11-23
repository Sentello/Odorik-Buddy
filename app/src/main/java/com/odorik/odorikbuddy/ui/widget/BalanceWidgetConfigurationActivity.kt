package com.odorik.odorikbuddy.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LanguagePreferences
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.ui.theme.OdorikBuddyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Saved settings of one balance widget, used to prefill the screen when reconfiguring. */
private data class BalanceWidgetConfig(
    val background: String,
    val textColor: String,
    val textSize: String,
    val showLogo: Boolean,
    /** The widget already shows a balance (reconfigure, not a first add). */
    val hasBalance: Boolean
)

@AndroidEntryPoint
class BalanceWidgetConfigurationActivity : ComponentActivity() {

    // Follow the app's language, not the phone's (same as MainActivity).
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguagePreferences.wrapContext(newBase))
    }

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var balanceWidgetRefresher: BalanceWidgetRefresher

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    // Loaded from the widget's saved state; null until loaded. A new widget gets the defaults.
    private var initialConfig by mutableStateOf<BalanceWidgetConfig?>(null)

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

        // Prefill from saved state so reconfiguring an existing widget keeps its settings.
        lifecycleScope.launch {
            initialConfig = try {
                val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(appWidgetId)
                val prefs = getAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, glanceId)
                BalanceWidgetConfig(
                    background = prefs[BalanceWidget.backgroundKey] ?: DEFAULT_CONFIG.background,
                    textColor = prefs[BalanceWidget.textColorKey] ?: DEFAULT_CONFIG.textColor,
                    textSize = prefs[BalanceWidget.textSizeKey] ?: DEFAULT_CONFIG.textSize,
                    showLogo = prefs[BalanceWidget.showLogoKey] ?: DEFAULT_CONFIG.showLogo,
                    hasBalance = prefs[BalanceWidget.balanceKey] != null
                )
            } catch (e: Exception) {
                android.util.Log.w("BalanceWidgetConfig", "Could not read widget state, using defaults", e)
                DEFAULT_CONFIG
            }
        }

        setContent {
            OdorikBuddyTheme(themeManager = themeManager) {
                val initial = initialConfig
                if (initial == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    ConfigurationScreen(
                        initial = initial,
                        onSave = { background, textColor, textSize, showLogo ->
                            saveWidgetState(background, textColor, textSize, showLogo, initial.hasBalance)
                        }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ConfigurationScreen(
        initial: BalanceWidgetConfig,
        onSave: (String, String, String, Boolean) -> Unit
    ) {
        var selectedBackground by remember { mutableStateOf(initial.background) }
        var selectedTextColor by remember { mutableStateOf(initial.textColor) }
        var selectedTextSize by remember { mutableStateOf(initial.textSize) }
        var showLogo by remember { mutableStateOf(initial.showLogo) }

        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.balance_widget_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Preview Section
                item {
                    Text(
                        text = stringResource(R.string.widget_preview),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Simulated Widget Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when (selectedBackground) {
                                    "SYSTEM" -> MaterialTheme.colorScheme.surfaceVariant
                                    "TRANSPARENT" -> Color.Transparent
                                    "TRANSLUCENT" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    "DARK" -> Color(0xFF1E1E1E)
                                    "LIGHT" -> Color(0xFFF5F5F5)
                                    "ODORIK" -> Color(0xFFC00012)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .border(
                                1.dp, 
                                if(selectedBackground == "TRANSPARENT") MaterialTheme.colorScheme.outline else Color.Transparent, 
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Dummy Widget Content
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Same logo and size as the real widget (BalanceWidget).
                            if (showLogo) {
                                Image(
                                    painter = painterResource(R.drawable.ic_buddy_symbol),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(
                                    text = "1 250,00 Kč",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = when(selectedTextSize) {
                                        "NORMAL" -> MaterialTheme.typography.titleLarge.fontSize
                                        "LARGE" -> MaterialTheme.typography.headlineMedium.fontSize
                                        "EXTRA_LARGE" -> MaterialTheme.typography.displaySmall.fontSize
                                        else -> MaterialTheme.typography.titleLarge.fontSize
                                    },
                                    color = when (selectedTextColor) {
                                        "AUTO" -> if (selectedBackground == "DARK" || selectedBackground == "ODORIK") Color.White else if (selectedBackground == "TRANSLUCENT" || selectedBackground == "LIGHT") Color.Black else MaterialTheme.colorScheme.onSurface
                                        "WHITE" -> Color.White
                                        "BLACK" -> Color.Black
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = "12:45",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (selectedTextColor) {
                                        "AUTO" -> if (selectedBackground == "DARK" || selectedBackground == "ODORIK") Color.White.copy(0.7f) else if (selectedBackground == "TRANSLUCENT" || selectedBackground == "LIGHT") Color.Black.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        "WHITE" -> Color.White.copy(0.7f)
                                        "BLACK" -> Color.Black.copy(0.7f)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }

                // Background Settings
                item {
                    ConfigSection(title = stringResource(R.string.widget_config_background)) {
                        val backgrounds = listOf(
                            "SYSTEM" to stringResource(R.string.widget_config_system),
                            "TRANSPARENT" to stringResource(R.string.widget_config_transparent),
                            "TRANSLUCENT" to stringResource(R.string.widget_config_translucent),
                            "DARK" to stringResource(R.string.widget_config_dark),
                            "LIGHT" to stringResource(R.string.widget_config_light),
                            "ODORIK" to stringResource(R.string.widget_style_odorik)
                        )
                        FlowRowCompat {
                            backgrounds.forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedBackground == key,
                                    onClick = { selectedBackground = key },
                                    label = { Text(label) },
                                    leadingIcon = if (selectedBackground == key) {
                                        { Icon(Icons.Default.Check, null) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // Text Color Settings
                item {
                    ConfigSection(title = stringResource(R.string.text_color)) {
                         val colors = listOf(
                            "AUTO" to stringResource(R.string.widget_config_auto),
                            "WHITE" to stringResource(R.string.widget_config_white),
                            "BLACK" to stringResource(R.string.widget_config_black)
                        )
                        FlowRowCompat {
                             colors.forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedTextColor == key,
                                    onClick = { selectedTextColor = key },
                                    label = { Text(label) },
                                    leadingIcon = if (selectedTextColor == key) {
                                        { Icon(Icons.Default.Check, null) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // Text Size Settings
                item {
                     ConfigSection(title = stringResource(R.string.widget_config_text_size)) {
                         val sizes = listOf(
                            "NORMAL" to stringResource(R.string.widget_config_normal),
                            "LARGE" to stringResource(R.string.widget_config_large),
                            "EXTRA_LARGE" to stringResource(R.string.widget_config_extra_large)
                        )
                        FlowRowCompat {
                             sizes.forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedTextSize == key,
                                    onClick = { selectedTextSize = key },
                                    label = { Text(label) },
                                    leadingIcon = if (selectedTextSize == key) {
                                        { Icon(Icons.Default.Check, null) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // Logo Settings
                item {
                    ConfigSection(title = stringResource(R.string.widget_config_logo)) {
                        val logoOptions = listOf(
                            true to stringResource(R.string.widget_config_logo_show),
                            false to stringResource(R.string.widget_config_logo_hide)
                        )
                        FlowRowCompat {
                            logoOptions.forEach { (key, label) ->
                                FilterChip(
                                    selected = showLogo == key,
                                    onClick = { showLogo = key },
                                    label = { Text(label) },
                                    leadingIcon = if (showLogo == key) {
                                        { Icon(Icons.Default.Check, null) }
                                    } else null
                                )
                            }
                        }
                        if (!showLogo) {
                            Text(
                                text = stringResource(R.string.widget_config_logo_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Save Button (respects navigation bar insets)
                item {
                    Button(
                        onClick = { onSave(selectedBackground, selectedTextColor, selectedTextSize, showLogo) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp)
                            .navigationBarsPadding()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }

    @Composable
    fun ConfigSection(title: String, content: @Composable () -> Unit) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
    
    /** Option chips that wrap onto the next line, so every choice is visible without scrolling. */
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlowRowCompat(content: @Composable () -> Unit) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }

    private fun saveWidgetState(
        background: String,
        textColor: String,
        textSize: String,
        showLogo: Boolean,
        hasBalance: Boolean
    ) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(appWidgetId)
            
            // 1. Save config
            updateAppWidgetState(applicationContext, glanceId) { prefs ->
                prefs[BalanceWidget.backgroundKey] = background
                prefs[BalanceWidget.textColorKey] = textColor
                prefs[BalanceWidget.textSizeKey] = textSize
                prefs[BalanceWidget.showLogoKey] = showLogo
            }

            if (hasBalance) {
                // 2a. Reconfigure: keep the balance, only redraw (no network, works offline)
                BalanceWidget().update(applicationContext, glanceId)
            } else {
                // 2b. New widget: fetch the balance right away (shows "…" while loading)
                balanceWidgetRefresher.refresh(glanceId, showProgress = true)
            }

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }

    private companion object {
        val DEFAULT_CONFIG = BalanceWidgetConfig(
            background = "SYSTEM",
            textColor = "AUTO",
            textSize = "NORMAL",
            showLogo = true,
            hasBalance = false
        )
    }
}
