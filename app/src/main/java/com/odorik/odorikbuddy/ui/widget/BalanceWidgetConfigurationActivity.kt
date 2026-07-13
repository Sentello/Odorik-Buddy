package com.odorik.odorikbuddy.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase
import com.odorik.odorikbuddy.ui.theme.OdorikBuddyTheme
import com.odorik.odorikbuddy.ui.theme.SettingsAccent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BalanceWidgetConfigurationActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var getCreditUseCase: GetCreditUseCase

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

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

        setContent {
            OdorikBuddyTheme(themeManager = themeManager) {
                ConfigurationScreen(
                    onSave = { background, textColor, textSize ->
                        saveWidgetState(background, textColor, textSize)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ConfigurationScreen(
        onSave: (String, String, String) -> Unit
    ) {
        var selectedBackground by remember { mutableStateOf("SYSTEM") }
        var selectedTextColor by remember { mutableStateOf("AUTO") }
        var selectedTextSize by remember { mutableStateOf("NORMAL") }

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
                             Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SettingsAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("O", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.size(16.dp))
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
                                        "AUTO" -> if (selectedBackground == "DARK") Color.White else MaterialTheme.colorScheme.onSurface
                                        "WHITE" -> Color.White
                                        "BLACK" -> Color.Black
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = "12:45",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (selectedTextColor) {
                                        "AUTO" -> if (selectedBackground == "DARK") Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
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
                            "LIGHT" to stringResource(R.string.widget_config_light)
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

                // Save Button (respects navigation bar insets)
                item {
                    Button(
                        onClick = { onSave(selectedBackground, selectedTextColor, selectedTextSize) },
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
    
    // Simple replacement for FlowRow since we might be on older Compose
    @Composable
    fun FlowRowCompat(content: @Composable () -> Unit) {
        // Just use a wrapped Row for now as FlowRow is experimental in some versions
        // Or simple Column of Rows if items are many. 
        // Given the limited items, a scrollable Row is safer or a simple custom layout.
        // Let's use a LazyRow for horizontal scrolling if it overflows
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    content()
                }
            }
        }
    }

    private fun saveWidgetState(background: String, textColor: String, textSize: String) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(appWidgetId)
            
            // 1. Save config and set loading state
            updateAppWidgetState(applicationContext, glanceId) { prefs ->
                prefs[BalanceWidget.backgroundKey] = background
                prefs[BalanceWidget.textColorKey] = textColor
                prefs[BalanceWidget.textSizeKey] = textSize
                prefs[BalanceWidget.isLoadingKey] = true
                prefs.remove(BalanceWidget.errorKey)
            }
            BalanceWidget().update(applicationContext, glanceId)
            
            // 2. Fetch data immediately
            val result = getCreditUseCase.execute()
            
            // 3. Update state with result
            updateAppWidgetState(applicationContext, glanceId) { prefs ->
                prefs[BalanceWidget.isLoadingKey] = false
                prefs[BalanceWidget.lastUpdatedKey] = System.currentTimeMillis()
                
                result.onSuccess { balance ->
                    prefs[BalanceWidget.balanceKey] = balance
                    prefs.remove(BalanceWidget.errorKey)
                }.onFailure { error ->
                    prefs[BalanceWidget.errorKey] = error.message ?: applicationContext.getString(R.string.error_unknown_generic)
                }
            }
            BalanceWidget().update(applicationContext, glanceId)
            
            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}
