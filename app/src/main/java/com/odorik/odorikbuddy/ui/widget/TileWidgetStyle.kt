package com.odorik.odorikbuddy.ui.widget

enum class TileWidgetStyle {
    SQUARE,
    CIRCLE;

    companion object {
        fun fromKey(key: String?): TileWidgetStyle =
            values().firstOrNull { it.name == key } ?: SQUARE
    }
}
