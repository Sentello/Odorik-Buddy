package com.odorik.odorikbuddy.ui.calls

/**
 * Size of the tiles on the Calls screen (Settings > Tile size). The enum name is the
 * stored value. Same three steps as the widget's text size.
 */
enum class TileSize(
    /** One tile per row on a phone (one column fewer than usual on a tablet). */
    val fewerColumns: Boolean,
    val titleMaxLines: Int
) {
    /** The classic grid: small text, fixed-height tiles. */
    NORMAL(fewerColumns = false, titleMaxLines = 1),

    /** One per row, moderately bigger text, a low tile with the badge beside the text. */
    LARGE(fewerColumns = true, titleMaxLines = 2),

    /** One per row, the biggest text, a tall tile with the badge above the text. */
    EXTRA_LARGE(fewerColumns = true, titleMaxLines = 2);

    companion object {
        /** Unknown or missing values fall back to NORMAL, the look before this setting. */
        fun fromKey(key: String?): TileSize =
            values().firstOrNull { it.name == key } ?: NORMAL
    }
}
