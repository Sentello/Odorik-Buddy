package com.odorik.odorikbuddy.ui.widget

object TileWidgetTexts {

    const val SUBTITLE_ALPHA = 0.8f

    private val whitespace = Regex("\\s+")

    fun title(label: String, contactName: String): String =
        label.ifEmpty { contactName }

    fun subtitle(label: String, contactName: String, recipient: String): String = when {
        label.isNotEmpty() -> contactName
        contactName != recipient -> recipient
        else -> ""
    }

    fun initials(name: String): String {
        val parts = name.trim().split(whitespace).filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> "?"
            parts.size >= 2 -> (parts[0].take(1) + parts[1].take(1)).uppercase()
            else -> parts[0].take(1).uppercase()
        }
    }
}
