package com.odorik.odorikbuddy.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object Motion {
    val emphasizedSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    const val durShort = 90
    const val durMedium = 220
}
