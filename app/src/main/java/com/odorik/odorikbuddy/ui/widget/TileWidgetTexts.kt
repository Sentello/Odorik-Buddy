package com.odorik.odorikbuddy.ui.widget

/**
 * Texts shown on the quick-dial (tile) widget. Shared by the real widget and the
 * config-screen preview so the two can't drift apart.
 */
object TileWidgetTexts {

    /** Opacity of the subtitle relative to the title colour. */
    const val SUBTITLE_ALPHA = 0.8f

    private val whitespace = Regex("\\s+")

    /** The tile's label, or the contact name when the tile has no label. */
    fun title(label: String, contactName: String): String =
        label.ifEmpty { contactName }

    /**
     * Second line: the contact name under a label, or the number under a contact name.
     * Empty when it would only repeat the title.
     */
    fun subtitle(label: String, contactName: String, recipient: String): String = when {
        label.isNotEmpty() -> contactName
        contactName != recipient -> recipient
        else -> ""
    }

    /** Up to two initials for the CIRCLE style, "?" when there is nothing to show. */
    fun initials(name: String): String {
        val parts = name.trim().split(whitespace).filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> "?"
            parts.size >= 2 -> (parts[0].take(1) + parts[1].take(1)).uppercase()
            else -> parts[0].take(1).uppercase()
        }
    }
}
