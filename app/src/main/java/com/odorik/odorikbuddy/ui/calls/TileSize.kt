package com.odorik.odorikbuddy.ui.calls

enum class TileSize(
    val fewerColumns: Boolean,
    val titleMaxLines: Int
) {
    NORMAL(fewerColumns = false, titleMaxLines = 1),

    LARGE(fewerColumns = true, titleMaxLines = 2),

    EXTRA_LARGE(fewerColumns = true, titleMaxLines = 2);

    companion object {
        fun fromKey(key: String?): TileSize =
            values().firstOrNull { it.name == key } ?: NORMAL
    }
}
