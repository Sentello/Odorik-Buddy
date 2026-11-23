package com.odorik.odorikbuddy.ui.widget

/** Shape of the quick-dial (tile) widget. The enum name is the value stored in the widget state. */
enum class TileWidgetStyle {
    SQUARE,
    CIRCLE;

    companion object {
        /** Unknown or missing values fall back to SQUARE, the original look. */
        fun fromKey(key: String?): TileWidgetStyle =
            values().firstOrNull { it.name == key } ?: SQUARE
    }
}
