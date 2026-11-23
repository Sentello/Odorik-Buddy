package com.odorik.odorikbuddy.ui.widget

/**
 * Text sizes for the quick-dial (tile) widget. The stored key matches the
 * balance widget's "text_size" values so both widgets offer the same choices.
 */
enum class TileWidgetTextSize(
    val titleSp: Int,
    val subtitleSp: Int,
    val initialsSp: Int,
    val titleMaxLines: Int
) {
    NORMAL(titleSp = 12, subtitleSp = 10, initialsSp = 24, titleMaxLines = 1),
    LARGE(titleSp = 16, subtitleSp = 13, initialsSp = 28, titleMaxLines = 2),
    EXTRA_LARGE(titleSp = 20, subtitleSp = 16, initialsSp = 32, titleMaxLines = 2);

    /**
     * Whether the subtitle fits under a full-height title in a widget that is
     * [availableHeightDp] tall. A 1x1 home-screen cell is only about 57-73dp, too little for
     * a two-line large title plus a subtitle, so the widget then shows the title alone
     * instead of clipping both. [fontScale] is the phone's font-size setting.
     */
    fun subtitleFits(availableHeightDp: Float, fontScale: Float): Boolean {
        val textHeight = (titleMaxLines * titleSp + subtitleSp) * LINE_HEIGHT_FACTOR * fontScale
        return textHeight + VERTICAL_PADDING_DP <= availableHeightDp
    }

    companion object {
        /** Approximate line height of the widget font relative to its size. */
        private const val LINE_HEIGHT_FACTOR = 1.2f

        /** The widget's 4dp padding, top and bottom. */
        private const val VERTICAL_PADDING_DP = 8f

        /** Unknown or missing values fall back to NORMAL, so existing widgets look unchanged. */
        fun fromKey(key: String?): TileWidgetTextSize =
            values().firstOrNull { it.name == key } ?: NORMAL
    }
}
