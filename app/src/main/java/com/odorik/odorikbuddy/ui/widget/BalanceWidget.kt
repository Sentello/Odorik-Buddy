package com.odorik.odorikbuddy.ui.widget

import android.content.Context
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


        val backgroundKey = stringPreferencesKey("bg_style")
        val textColorKey = stringPreferencesKey("text_color")
        val textSizeKey = stringPreferencesKey("text_size")
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

                WidgetContent(
                    balance = balance,
                    lastUpdated = lastUpdated,
                    isLoading = isLoading,
                    error = error,
                    bgStyle = bgStyle,
                    textColorStyle = textColorStyle,
                    textSizeStyle = textSizeStyle,
                    context = localizedContext
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        balance: Double?,
        lastUpdated: Long?,
        isLoading: Boolean,
        error: String?,
        bgStyle: String,
        textColorStyle: String,
        textSizeStyle: String,
        context: Context
    ) {
        val currencyFormatter = CurrencyFormatter(context)
        val language = context.resources.configuration.locales[0].language




        val bgColorProvider = when (bgStyle) {
            "SYSTEM" -> GlanceTheme.colors.surfaceVariant
            "TRANSPARENT" -> ColorProvider(Color.Transparent)
            "TRANSLUCENT" -> ColorProvider(Color.White.copy(alpha = 0.7f))
            "DARK" -> ColorProvider(Color(0xFF1E1E1E))
            "LIGHT" -> ColorProvider(Color(0xFFF5F5F5))
            else -> GlanceTheme.colors.surface
        }


        val mainTextColor = when (textColorStyle) {
            "WHITE" -> ColorProvider(Color.White)
            "BLACK" -> ColorProvider(Color.Black)
            "AUTO" -> if (bgStyle == "DARK") ColorProvider(Color.White) else if (bgStyle == "LIGHT") ColorProvider(Color.Black) else GlanceTheme.colors.onSurface
            else -> GlanceTheme.colors.onSurface
        }

        val secondaryTextColor = when (textColorStyle) {
             "WHITE" -> ColorProvider(Color.White.copy(0.7f))
             "BLACK" -> ColorProvider(Color.Black.copy(0.7f))
             "AUTO" -> if (bgStyle == "DARK") ColorProvider(Color.White.copy(0.7f)) else if (bgStyle == "LIGHT") ColorProvider(Color.Black.copy(0.6f)) else GlanceTheme.colors.onSurfaceVariant
             else -> GlanceTheme.colors.onSurfaceVariant
        }


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

                Image(
                    provider = ImageProvider(R.drawable.ic_odorik_logo),
                    contentDescription = "Odorik Logo",
                    modifier = GlanceModifier.size(48.dp)
                )

                Spacer(modifier = GlanceModifier.width(16.dp))


                Column(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.fillMaxSize()
                ) {

                    if (isLoading) {
                        Text(
                            text = context.getString(R.string.loading),
                            style = TextStyle(
                                color = mainTextColor,
                                fontSize = 16.sp
                            )
                        )
                    } else if (error != null) {
                         Text(
                            text = context.getString(R.string.retry),
                            style = TextStyle(
                                color = GlanceTheme.colors.error,
                                fontSize = 16.sp,
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


                    if (!isLoading && lastUpdated != null) {
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        Text(
                            text = sdf.format(Date(lastUpdated)),
                            style = TextStyle(
                                color = secondaryTextColor,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}