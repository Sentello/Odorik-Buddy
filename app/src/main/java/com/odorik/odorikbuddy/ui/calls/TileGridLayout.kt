package com.odorik.odorikbuddy.ui.calls

object TileGridLayout {

    fun columns(standardColumns: Int, size: TileSize): Int =
        (if (size.fewerColumns) standardColumns - 1 else standardColumns).coerceAtLeast(1)
}
