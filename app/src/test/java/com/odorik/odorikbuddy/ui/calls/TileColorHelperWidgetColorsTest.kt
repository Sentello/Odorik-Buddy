package com.odorik.odorikbuddy.ui.calls

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TileColorHelperWidgetColorsTest {

    // Red as stored by the tile dialog (taken from the palette, incl. its historical extra "FF")
    private val red = TileColorHelper.allBaseColors.first { Color(it).toArgb() == 0xFFFFEBEE.toInt() }

    @Test
    fun `red tile is pale in light mode and strong red in dark mode, like the in-app tile`() {
        val light = TileColorHelper.widgetColors(red, null, isDark = false)
        val dark = TileColorHelper.widgetColors(red, null, isDark = true)

        assertEquals(0xFFFFEBEE.toInt(), light.background.toArgb())
        assertEquals(0xFFB71C1C.toInt(), dark.background.toArgb())
        assertEquals(TileColorHelper.resolveColor(red, false), light.background)
        assertEquals(TileColorHelper.resolveColor(red, true), dark.background)
    }

    @Test
    fun `automatic text is black in light mode and white in dark mode`() {
        assertEquals(Color.Black, TileColorHelper.widgetColors(red, null, isDark = false).text)
        assertEquals(Color.White, TileColorHelper.widgetColors(red, null, isDark = true).text)
    }

    @Test
    fun `readable explicit text colour is kept`() {
        val blue = 0xFF0D47A1
        assertEquals(Color(blue), TileColorHelper.widgetColors(red, blue, isDark = false).text)
        // black text on the dark-mode amber background is still easy to read
        val amber = 0xFFFFF8E1
        assertEquals(Color.Black, TileColorHelper.widgetColors(amber, 0xFF000000, isDark = true).text)
    }

    @Test
    fun `explicit text colour equal to the dark background is replaced`() {
        val blueTile = 0xFFE3F2FD
        val blueText = 0xFF0D47A1 // same as the blue tile's dark-mode background
        assertEquals(Color(blueText), TileColorHelper.widgetColors(blueTile, blueText, isDark = false).text)
        assertEquals(Color.White, TileColorHelper.widgetColors(blueTile, blueText, isDark = true).text)
    }

    @Test
    fun `text never has the same colour as the background`() {
        val textChoices = listOf<Long?>(null) + TileColorHelper.textColors
        (listOf<Long?>(null) + TileColorHelper.allBaseColors).forEach { base ->
            textChoices.forEach { text ->
                listOf(false, true).forEach { isDark ->
                    val colors = TileColorHelper.widgetColors(base, text, isDark)
                    val lighter = maxOf(colors.text.luminance(), colors.background.luminance())
                    val darker = minOf(colors.text.luminance(), colors.background.luminance())
                    assertTrue(
                        "base=${base?.toString(16)} text=${text?.toString(16)} dark=$isDark",
                        (lighter + 0.05f) / (darker + 0.05f) >= 2f
                    )
                }
            }
        }
    }

    @Test
    fun `automatic text stays automatic`() {
        assertEquals(null, TileColorHelper.readableTextColor(null, Color.Black))
    }

    @Test
    fun `no colour falls back to neutral grey with readable text`() {
        val light = TileColorHelper.widgetColors(null, null, isDark = false)
        val dark = TileColorHelper.widgetColors(null, null, isDark = true)

        assertEquals(0xFFECEFF1.toInt(), light.background.toArgb())
        assertEquals(Color.Black, light.text)
        assertEquals(0xFF263238.toInt(), dark.background.toArgb())
        assertEquals(Color.White, dark.text)
    }

    @Test
    fun `shadow only behind light text`() {
        assertEquals(Color.Transparent, TileColorHelper.widgetColors(red, null, isDark = false).shadow)
        assertNotEquals(Color.Transparent, TileColorHelper.widgetColors(red, null, isDark = true).shadow)
    }

    @Test
    fun `every palette colour has a different dark variant`() {
        TileColorHelper.allBaseColors.forEach { base ->
            val light = TileColorHelper.widgetColors(base, null, isDark = false).background
            val dark = TileColorHelper.widgetColors(base, null, isDark = true).background
            assertNotEquals("palette colour ${base.toString(16)}", light, dark)
        }
    }
}
