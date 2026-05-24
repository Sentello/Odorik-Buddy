package com.odorik.odorikbuddy.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

@Composable
fun ConfigureBottomSheetWindow() {
    val view = LocalView.current
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.parent as? DialogWindowProvider)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightNavigationBars = !isDarkTheme
            }
        }
    }
}
