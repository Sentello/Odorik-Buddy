package com.odorik.odorikbuddy.ui.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TileWidgetTextSizeTest {

    @Test
    fun `missing key falls back to NORMAL`() {
        assertEquals(TileWidgetTextSize.NORMAL, TileWidgetTextSize.fromKey(null))
    }

    @Test
    fun `empty or unknown key falls back to NORMAL`() {
        assertEquals(TileWidgetTextSize.NORMAL, TileWidgetTextSize.fromKey(""))
        assertEquals(TileWidgetTextSize.NORMAL, TileWidgetTextSize.fromKey("HUGE"))
    }

    @Test
    fun `known keys resolve to their size`() {
        assertEquals(TileWidgetTextSize.LARGE, TileWidgetTextSize.fromKey("LARGE"))
        assertEquals(TileWidgetTextSize.EXTRA_LARGE, TileWidgetTextSize.fromKey("EXTRA_LARGE"))
    }

    @Test
    fun `sizes grow strictly from NORMAL to EXTRA_LARGE`() {
        val (n, l, xl) = listOf(
            TileWidgetTextSize.NORMAL,
            TileWidgetTextSize.LARGE,
            TileWidgetTextSize.EXTRA_LARGE
        )
        assertTrue(n.titleSp < l.titleSp && l.titleSp < xl.titleSp)
        assertTrue(n.subtitleSp < l.subtitleSp && l.subtitleSp < xl.subtitleSp)
        assertTrue(n.initialsSp < l.initialsSp && l.initialsSp < xl.initialsSp)
    }

    @Test
    fun `NORMAL keeps the original widget look`() {
        val normal = TileWidgetTextSize.NORMAL
        assertEquals(12, normal.titleSp)
        assertEquals(10, normal.subtitleSp)
        assertEquals(24, normal.initialsSp)
        assertEquals(1, normal.titleMaxLines)
    }

    @Test
    fun `NORMAL always shows the subtitle, even in the smallest widget`() {
        assertTrue(TileWidgetTextSize.NORMAL.subtitleFits(availableHeightDp = 40f, fontScale = 1f))
    }

    @Test
    fun `EXTRA_LARGE drops the subtitle in a 1x1 cell but keeps it in a taller widget`() {
        assertFalse(TileWidgetTextSize.EXTRA_LARGE.subtitleFits(availableHeightDp = 65f, fontScale = 1f))
        assertTrue(TileWidgetTextSize.EXTRA_LARGE.subtitleFits(availableHeightDp = 130f, fontScale = 1f))
    }

    @Test
    fun `a bigger phone font needs more room`() {
        assertTrue(TileWidgetTextSize.LARGE.subtitleFits(availableHeightDp = 65f, fontScale = 1f))
        assertFalse(TileWidgetTextSize.LARGE.subtitleFits(availableHeightDp = 65f, fontScale = 1.3f))
    }
}
