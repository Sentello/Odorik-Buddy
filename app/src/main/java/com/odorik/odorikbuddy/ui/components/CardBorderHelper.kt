package com.odorik.odorikbuddy.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp


fun Modifier.darkModeBorder(shape: Shape = RoundedCornerShape(12.dp)): Modifier = composed {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val borderColor = if (isDark) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    } else {
        androidx.compose.ui.graphics.Color.Transparent
    }

    this.then(
        Modifier.border(
            width = 1.dp,
            color = borderColor,
            shape = shape
        )
    )
}
