package com.odorik.odorikbuddy.ui.calls

/** Layout rules for the tile grid on the Calls screen. */
object TileGridLayout {

    /**
     * Number of tile columns. [standardColumns] is what the screen width normally gets
     * (2 on a phone, 3-4 on a tablet). The larger sizes use one column fewer, so a phone
     * shows one big tile per row.
     */
    fun columns(standardColumns: Int, size: TileSize): Int =
        (if (size.fewerColumns) standardColumns - 1 else standardColumns).coerceAtLeast(1)
}
