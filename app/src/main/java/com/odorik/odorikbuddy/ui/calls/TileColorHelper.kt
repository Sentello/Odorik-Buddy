package com.odorik.odorikbuddy.ui.calls

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.odorik.odorikbuddy.ui.calls.TileColorHelper.readableTextColor
import com.odorik.odorikbuddy.ui.calls.TileColorHelper.resolveColor

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
        // Extra "FF" is a historical typo. Keep it: saved red tiles store this exact value,
        // and Color(Long) uses only the low 32 bits, so it still displays as #FFEBEE.
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

    /** Neutral widget background when neither the tile nor the widget has a colour. */
    private const val NEUTRAL_LIGHT = 0xFFECEFF1
    private const val NEUTRAL_DARK = 0xFF263238

    /** Colours a quick-dial widget (and its preview) shows in one mode. */
    data class WidgetColors(val background: Color, val text: Color, val shadow: Color)

    /**
     * Single source of truth for widget colours, used by both the real widget and the
     * config-screen preview so they always match the in-app tile:
     * - [baseColor] is a stored palette colour (its light variant) or null for "no colour";
     *   in dark mode it is swapped for the strong variant, exactly like [resolveColor].
     * - [textColor] is an explicit user choice (see [readableTextColor]), or null for
     *   automatic: black in light mode, white in dark mode (same rule as the in-app tile).
     * - [WidgetColors.shadow] is a dark shadow behind light text only; dark text gets none.
     */
    fun widgetColors(baseColor: Long?, textColor: Long?, isDark: Boolean): WidgetColors {
        val background = resolveColor(baseColor, isDark)
            ?: Color(if (isDark) NEUTRAL_DARK else NEUTRAL_LIGHT)
        val text = readableTextColor(textColor, background) ?: if (isDark) Color.White else Color.Black
        val shadow = if (text.luminance() > 0.5f) Color.Black.copy(alpha = 0.6f) else Color.Transparent
        return WidgetColors(background, text, shadow)
    }

    /** Below this contrast ratio an explicit text colour counts as unreadable on its background. */
    private const val MIN_TEXT_CONTRAST = 2f

    /**
     * The user's explicit [textColor], unless it can't be read on [background] - e.g. blue
     * text on a blue tile, whose dark-mode background is that very same blue. Then black or
     * white is used instead, whichever stands out more. Null (automatic) stays null.
     */
    fun readableTextColor(textColor: Long?, background: Color): Color? {
        val text = Color(textColor ?: return null)
        if (contrast(text, background) >= MIN_TEXT_CONTRAST) return text
        return if (contrast(Color.White, background) >= contrast(Color.Black, background)) Color.White else Color.Black
    }

    /** WCAG contrast ratio, from 1 (same colour) to 21 (black on white). */
    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance()
        val lb = b.luminance()
        return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
    }
}
