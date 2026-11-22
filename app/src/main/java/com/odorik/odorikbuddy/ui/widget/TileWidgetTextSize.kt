package com.odorik.odorikbuddy.ui.widget

enum class TileWidgetTextSize(
    val titleSp: Int,
    val subtitleSp: Int,
    val initialsSp: Int,
    val titleMaxLines: Int
) {
    NORMAL(titleSp = 12, subtitleSp = 10, initialsSp = 24, titleMaxLines = 1),
    LARGE(titleSp = 16, subtitleSp = 13, initialsSp = 28, titleMaxLines = 2),
    EXTRA_LARGE(titleSp = 20, subtitleSp = 16, initialsSp = 32, titleMaxLines = 2);

    fun subtitleFits(availableHeightDp: Float, fontScale: Float): Boolean {
        val textHeight = (titleMaxLines * titleSp + subtitleSp) * LINE_HEIGHT_FACTOR * fontScale
        return textHeight + VERTICAL_PADDING_DP <= availableHeightDp
    }

    companion object {
        private const val LINE_HEIGHT_FACTOR = 1.2f

        private const val VERTICAL_PADDING_DP = 8f

        fun fromKey(key: String?): TileWidgetTextSize =
            values().firstOrNull { it.name == key } ?: NORMAL
    }
}
