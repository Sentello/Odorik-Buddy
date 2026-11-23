package com.odorik.odorikbuddy.data.local

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_NAV_ICON_STYLE = "nav_icon_style"
        private const val KEY_DARK_MODE = "dark_mode" // legacy
    }

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val themeMode: MutableState<ThemeMode> = mutableStateOf(loadThemeMode())
    val appTheme: MutableState<AppTheme> = mutableStateOf(loadAppTheme())
    val navIconStyle: MutableState<NavIconStyle> = mutableStateOf(loadNavIconStyle())

    private fun loadThemeMode(): ThemeMode {
        val stored = sharedPreferences.getString(KEY_THEME_MODE, null)
        if (stored != null) {
            return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.SYSTEM)
        }
        // Migrate legacy dark_mode boolean
        if (sharedPreferences.contains(KEY_DARK_MODE)) {
            val dark = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
            val migrated = if (dark) ThemeMode.DARK else ThemeMode.LIGHT
            sharedPreferences.edit()
                .putString(KEY_THEME_MODE, migrated.name)
                .remove(KEY_DARK_MODE)
                .apply()
            return migrated
        }
        return ThemeMode.SYSTEM
    }

    private fun loadAppTheme(): AppTheme {
        val stored = sharedPreferences.getString(KEY_APP_THEME, null)
            ?: return AppTheme.STANDARD
        return runCatching { AppTheme.valueOf(stored) }.getOrDefault(AppTheme.STANDARD)
    }

    private fun loadNavIconStyle(): NavIconStyle {
        val stored = sharedPreferences.getString(KEY_NAV_ICON_STYLE, null)
            ?: return NavIconStyle.CLASSIC
        return runCatching { NavIconStyle.valueOf(stored) }.getOrDefault(NavIconStyle.CLASSIC)
    }

    fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
        themeMode.value = mode
    }

    fun setAppTheme(theme: AppTheme) {
        sharedPreferences.edit().putString(KEY_APP_THEME, theme.name).apply()
        appTheme.value = theme
    }

    fun setNavIconStyle(style: NavIconStyle) {
        sharedPreferences.edit().putString(KEY_NAV_ICON_STYLE, style.name).apply()
        navIconStyle.value = style
    }
}
