package com.odorik.odorikbuddy.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.util.CurrencyFormatter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BalanceWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun localeManager(): LocaleManager
    }

    companion object {
        val balanceKey = doublePreferencesKey("balance")
        val lastUpdatedKey = longPreferencesKey("last_updated")
        val isLoadingKey = booleanPreferencesKey("is_loading")
        val errorKey = stringPreferencesKey("error")
        
        // Configuration Keys
        val backgroundKey = stringPreferencesKey("bg_style")
        val textColorKey = stringPreferencesKey("text_color")
        val textSizeKey = stringPreferencesKey("text_size")
        /** Missing means "show", so widgets from before 1.5.6 keep their logo. */
        val showLogoKey = booleanPreferencesKey("show_logo")

        /** Error reds for dark and light backgrounds (Material 3 error tones 80 and 40). */
        private val LIGHT_RED = Color(0xFFFFB4AB)
        private val DARK_RED = Color(0xFFB3261E)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            WidgetEntryPoint::class.java
        )
        val localeManager = entryPoint.localeManager()
        val localizedContext = localeManager.createLocaleContext(appContext)

        provideContent {
            GlanceTheme {
                val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
                val balance = prefs[balanceKey]
                val lastUpdated = prefs[lastUpdatedKey]
                val isLoading = prefs[isLoadingKey] ?: false
                val error = prefs[errorKey]
                
                val bgStyle = prefs[backgroundKey] ?: "SYSTEM"
                val textColorStyle = prefs[textColorKey] ?: "AUTO"
                val textSizeStyle = prefs[textSizeKey] ?: "NORMAL"
                val showLogo = prefs[showLogoKey] ?: true

                WidgetContent(
                    balance = balance,
                    lastUpdated = lastUpdated,
                    isLoading = isLoading,
                    error = error,
                    bgStyle = bgStyle,
                    textColorStyle = textColorStyle,
                    textSizeStyle = textSizeStyle,
                    showLogo = showLogo,
                    context = localizedContext
                )
            }
        }
    }

    /**
     * The launcher's own intent, so the widget behaves like the app icon: a running app comes
     * back where it was instead of opening a second copy.
     */
    private fun openAppIntent(context: Context): Intent =
        (context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, com.odorik.odorikbuddy.MainActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

    @Composable
    private fun WidgetContent(
        balance: Double?,
        lastUpdated: Long?,
        isLoading: Boolean,
        error: String?,
        bgStyle: String,
        textColorStyle: String,
        textSizeStyle: String,
        showLogo: Boolean,
        context: Context
    ) {
        val currencyFormatter = CurrencyFormatter(context)
        val language = context.resources.configuration.locales[0].language

        // --- Style Resolution ---
        
        // Background Color
        val bgColorProvider = when (bgStyle) {
            "SYSTEM" -> GlanceTheme.colors.surfaceVariant
            "TRANSPARENT" -> ColorProvider(Color.Transparent)
            "TRANSLUCENT" -> ColorProvider(Color.White.copy(alpha = 0.7f))
            "DARK" -> ColorProvider(Color(0xFF1E1E1E))
            "LIGHT" -> ColorProvider(Color(0xFFF5F5F5))
            "ODORIK" -> ColorProvider(Color(0xFFC00012))
            else -> GlanceTheme.colors.surface
        }

        // Text Color — TRANSLUCENT defaults to black for readability on light wallpapers
        val mainTextColor = when (textColorStyle) {
            "WHITE" -> ColorProvider(Color.White)
            "BLACK" -> ColorProvider(Color.Black)
            "AUTO" -> when (bgStyle) {
                "DARK", "ODORIK" -> ColorProvider(Color.White)
                "LIGHT", "TRANSLUCENT" -> ColorProvider(Color.Black)
                else -> GlanceTheme.colors.onSurface
            }
            else -> GlanceTheme.colors.onSurface
        }
        
        val secondaryTextColor = when (textColorStyle) {
             "WHITE" -> ColorProvider(Color.White.copy(0.7f))
             "BLACK" -> ColorProvider(Color.Black.copy(0.7f))
             "AUTO" -> when (bgStyle) {
                 "DARK", "ODORIK" -> ColorProvider(Color.White.copy(0.7f))
                 "LIGHT", "TRANSLUCENT" -> ColorProvider(Color.Black.copy(0.6f))
                 else -> GlanceTheme.colors.onSurfaceVariant
             }
             else -> GlanceTheme.colors.onSurfaceVariant
        }

        // Error / stale colour. GlanceTheme's error colour follows the system theme, not the
        // chosen background, so pick a red that is readable on the actual background.
        val errorTextColor = when {
            bgStyle == "ODORIK" -> mainTextColor // red on red would be unreadable
            textColorStyle == "WHITE" -> ColorProvider(LIGHT_RED)
            textColorStyle == "BLACK" -> ColorProvider(DARK_RED)
            bgStyle == "DARK" -> ColorProvider(LIGHT_RED)
            bgStyle == "LIGHT" || bgStyle == "TRANSLUCENT" -> ColorProvider(DARK_RED)
            else -> GlanceTheme.colors.error
        }

        // The last refresh failed but we still know a balance: show it, mark the time as stale.
        val isStale = !isLoading && error != null && balance != null

        // Text Sizes
        val balanceFontSize = when (textSizeStyle) {
            "NORMAL" -> 22.sp
            "LARGE" -> 28.sp
            "EXTRA_LARGE" -> 34.sp
            else -> 22.sp
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(bgColorProvider)
                .cornerRadius(16.dp)
                .clickable(actionRunCallback<RefreshBalanceAction>())
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo: opens the app like its launcher icon. Its own click overrides the
                // widget's refresh action, so the rest of the widget still refreshes.
                // Without the logo the whole widget refreshes.
                if (showLogo) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_buddy_symbol),
                        contentDescription = context.getString(R.string.a11y_widget_open_app),
                        modifier = GlanceModifier
                            .size(48.dp)
                            .clickable(actionStartActivity(openAppIntent(context)))
                    )

                    Spacer(modifier = GlanceModifier.width(16.dp))
                }

                // Content Column
                Column(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                     // Balance / Loading / Error
                    if (isLoading) {
                        Text(
                            text = if (balance != null) {
                                currencyFormatter.formatCurrency(balance, language) + " …"
                            } else {
                                "…"
                            },
                            style = TextStyle(
                                color = mainTextColor,
                                fontSize = balanceFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else if (error != null && balance == null) {
                        // Nothing to show yet; tapping the widget retries the fetch.
                        Text(
                            text = context.getString(R.string.widget_tap_to_retry),
                            style = TextStyle(
                                color = errorTextColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else if (balance != null) {
                        Text(
                            text = currencyFormatter.formatCurrency(balance, language),
                            style = TextStyle(
                                color = mainTextColor,
                                fontSize = balanceFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Text(
                            text = "--",
                             style = TextStyle(
                                color = mainTextColor,
                                fontSize = balanceFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    
                    // Timestamp (Small)
                    if (!isLoading && lastUpdated != null) {
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val time = sdf.format(Date(lastUpdated))
                        Text(
                            // The ⚠ keeps "may be outdated" visible without relying on colour.
                            text = if (isStale) "⚠ $time" else time,
                            style = TextStyle(
                                color = if (isStale) errorTextColor else secondaryTextColor,
                                fontSize = 11.sp,
                                fontWeight = if (isStale) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}