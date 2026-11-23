package com.odorik.odorikbuddy.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** Inset from the screen edges. Matches the Scaffold FAB slot so manually
 *  placed FABs line up with the ones in the Scaffold slot. */
val FabEdgePadding = 16.dp

/** Gap between two FABs sitting side by side. */
val FabSpacing = 12.dp

/** Space a scrolling list should reserve so its last item clears the FAB. */
val FabListBottomSpacing = 88.dp

private val FabIconSize = 24.dp

/**
 * The app's single floating action button style: circular, 56.dp (Material
 * default), with a press-scale animation. Used on every screen so the buttons
 * look and behave the same wherever they appear.
 */
@Composable
fun AppFab(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1f, label = "fabScale")

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        interactionSource = interactionSource,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = CircleShape
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(FabIconSize),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(FabIconSize))
        }
    }
}
