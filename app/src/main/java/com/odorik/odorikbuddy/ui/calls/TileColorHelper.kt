package com.odorik.odorikbuddy.ui.calls

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.unit.ColorProvider


object TileColorHelper {
    private val colorMap = mapOf(
        0xFFE0F7FA to 0xFF006064,
        0xFFE8F5E9 to 0xFF1B5E20,
        0xFFFFF3E0 to 0xFFBF360C,
        0xFFF3E5F5 to 0xFF4A148C,
        0xFFFFFFEBEE to 0xFFB71C1C,
        0xFFFFF8E1 to 0xFFF57F17,

        0xFFE3F2FD to 0xFF0D47A1,
        0xFFE8EAF6 to 0xFF1A237E,
        0xFFFCE4EC to 0xFF880E4F,
        0xFFF9FBE7 to 0xFF33691E,
        0xFFEFEBE9 to 0xFF3E2723,
        0xFFECEFF1 to 0xFF263238
    )

    val allBaseColors = colorMap.keys.toList()


    val textColors = listOf(
        0xFF000000,
        0xFFFFFFFF,
        0xFFB71C1C,
        0xFF0D47A1,
        0xFF1B5E20,
        0xFFF57F17
    )


    fun resolveColor(baseColor: Long?, isDark: Boolean): Color? {
        if (baseColor == null) return null

        return if (isDark) {
            val darkColor = colorMap[baseColor] ?: baseColor
            Color(darkColor)
        } else {
            Color(baseColor)
        }
    }


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


    fun resolveWidgetTextColor(textColor: Long?): ColorProvider? {
        return textColor?.let { ColorProvider(Color(it)) }
    }
}
