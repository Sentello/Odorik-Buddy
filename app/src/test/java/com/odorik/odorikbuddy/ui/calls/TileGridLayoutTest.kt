package com.odorik.odorikbuddy.ui.calls

import org.junit.Assert.assertEquals
import org.junit.Test

class TileGridLayoutTest {

    @Test
    fun `normal tiles keep the usual column count`() {
        assertEquals(2, TileGridLayout.columns(standardColumns = 2, size = TileSize.NORMAL))
        assertEquals(4, TileGridLayout.columns(standardColumns = 4, size = TileSize.NORMAL))
    }

    @Test
    fun `large and extra large tiles are one per row on a phone`() {
        assertEquals(1, TileGridLayout.columns(standardColumns = 2, size = TileSize.LARGE))
        assertEquals(1, TileGridLayout.columns(standardColumns = 2, size = TileSize.EXTRA_LARGE))
    }

    @Test
    fun `larger tiles use one column fewer on a tablet`() {
        assertEquals(2, TileGridLayout.columns(standardColumns = 3, size = TileSize.LARGE))
        assertEquals(3, TileGridLayout.columns(standardColumns = 4, size = TileSize.EXTRA_LARGE))
    }

    @Test
    fun `there is always at least one column`() {
        assertEquals(1, TileGridLayout.columns(standardColumns = 1, size = TileSize.EXTRA_LARGE))
        assertEquals(1, TileGridLayout.columns(standardColumns = 0, size = TileSize.NORMAL))
    }
}
