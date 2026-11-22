package com.odorik.odorikbuddy.ui.calls

import org.junit.Assert.assertEquals
import org.junit.Test

class TileSizeTest {

    @Test
    fun `missing or unknown key is NORMAL, so nothing changes for existing users`() {
        assertEquals(TileSize.NORMAL, TileSize.fromKey(null))
        assertEquals(TileSize.NORMAL, TileSize.fromKey(""))
        assertEquals(TileSize.NORMAL, TileSize.fromKey("HUGE"))
    }

    @Test
    fun `stored keys resolve to their size`() {
        TileSize.values().forEach { assertEquals(it, TileSize.fromKey(it.name)) }
    }

    @Test
    fun `NORMAL keeps the classic one-line title`() {
        assertEquals(1, TileSize.NORMAL.titleMaxLines)
        assertEquals(false, TileSize.NORMAL.fewerColumns)
    }
}
