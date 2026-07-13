package com.odorik.odorikbuddy.ui.calls

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.unit.ColorProvider

/**
 * Shared color helper for both in-app tiles (Calls tab) and home screen widgets.
 * Provides a consistent palette and dark mode adaptation.
 */
object TileColorHelper {
    private val colorMap = mapOf(
        0xFFE0F7FA to 0xFF006064, // Cyan
        0xFFE8F5E9 to 0xFF1B5E20, // Green
        0xFFFFF3E0 to 0xFFBF360C, // Deep Orange
        0xFFF3E5F5 to 0xFF4A148C, // Purple
        0xFFFFFFEBEE to 0xFFB71C1C, // Red
        0xFFFFF8E1 to 0xFFF57F17, // Amber
        // New Colors
        0xFFE3F2FD to 0xFF0D47A1, // Blue
        0xFFE8EAF6 to 0xFF1A237E, // Indigo
        0xFFFCE4EC to 0xFF880E4F, // Pink
        0xFFF9FBE7 to 0xFF33691E, // Lime
        0xFFEFEBE9 to 0xFF3E2723, // Brown
        0xFFECEFF1 to 0xFF263238  // Blue Grey
    )

    val allBaseColors = colorMap.keys.toList()

    // Simple palette for Text Colors (used in both in-app and widget customizers)
    val textColors = listOf(
        0xFF000000, // Black
        0xFFFFFFFF, // White
        0xFFB71C1C, // Red
        0xFF0D47A1, // Blue
        0xFF1B5E20, // Green
        0xFFF57F17  // Amber/Yellow-ish
    )

    /**
     * Resolves a stored base color to the appropriate light or dark variant.
     * Used primarily by in-app TileItem.
     */
    fun resolveColor(baseColor: Long?, isDark: Boolean): Color? {
        if (baseColor == null) return null

        return if (isDark) {
            val darkColor = colorMap[baseColor] ?: baseColor
            Color(darkColor)
        } else {
            Color(baseColor)
        }
    }

    /**
     * For Glance widgets (no access to isSystemInDarkTheme in the same way).
     * Returns the raw color or falls back to a sensible GlanceTheme color.
     * Widget overrides should generally store the final color the user picked.
     */
    @Composable
    fun resolveWidgetBackgroundColor(color: Long?, isDarkModePreferred: Boolean = false): ColorProvider {
        if (color == null) {
            return if (isDarkModePreferred) GlanceTheme.colors.surfaceVariant else GlanceTheme.colors.surface
        }
        val resolved = if (isDarkModePreferred) {
            colorMap[color] ?: color
        } else {
            color
        }
        return ColorProvider(Color(resolved))
    }

    /**
     * Simple text color resolver for widgets when user provides an explicit text color.
     */
    fun resolveWidgetTextColor(textColor: Long?): ColorProvider? {
        return textColor?.let { ColorProvider(Color(it)) }
    }
}
