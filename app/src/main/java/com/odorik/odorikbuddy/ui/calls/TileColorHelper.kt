package com.odorik.odorikbuddy.ui.calls

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

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

    private const val NEUTRAL_LIGHT = 0xFFECEFF1
    private const val NEUTRAL_DARK = 0xFF263238

    data class WidgetColors(val background: Color, val text: Color, val shadow: Color)

    fun widgetColors(baseColor: Long?, textColor: Long?, isDark: Boolean): WidgetColors {
        val background = resolveColor(baseColor, isDark)
            ?: Color(if (isDark) NEUTRAL_DARK else NEUTRAL_LIGHT)
        val text = readableTextColor(textColor, background) ?: if (isDark) Color.White else Color.Black
        val shadow = if (text.luminance() > 0.5f) Color.Black.copy(alpha = 0.6f) else Color.Transparent
        return WidgetColors(background, text, shadow)
    }

    private const val MIN_TEXT_CONTRAST = 2f

    fun readableTextColor(textColor: Long?, background: Color): Color? {
        val text = Color(textColor ?: return null)
        if (contrast(text, background) >= MIN_TEXT_CONTRAST) return text
        return if (contrast(Color.White, background) >= contrast(Color.Black, background)) Color.White else Color.Black
    }

    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance()
        val lb = b.luminance()
        return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
    }
}
