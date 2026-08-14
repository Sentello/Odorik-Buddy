package com.odorik.odorikbuddy.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppDimens(
    val screenPaddingHorizontal: Dp,
    val screenPaddingVertical: Dp,
    val spacing: Dp,
    val cardPadding: Dp,
    val contentMaxWidth: Dp
) {
    val screenPadding: PaddingValues
        get() = PaddingValues(
            horizontal = screenPaddingHorizontal,
            vertical = screenPaddingVertical
        )
}

val CompactDimens = AppDimens(
    screenPaddingHorizontal = 16.dp,
    screenPaddingVertical = 8.dp,
    spacing = 16.dp,
    cardPadding = 16.dp,
    contentMaxWidth = Dp.Infinity
)

val MediumDimens = AppDimens(
    screenPaddingHorizontal = 24.dp,
    screenPaddingVertical = 12.dp,
    spacing = 24.dp,
    cardPadding = 20.dp,
    contentMaxWidth = 640.dp
)

val ExpandedDimens = AppDimens(
    screenPaddingHorizontal = 24.dp,
    screenPaddingVertical = 12.dp,
    spacing = 24.dp,
    cardPadding = 20.dp,
    contentMaxWidth = 840.dp
)

val LocalAppDimens = staticCompositionLocalOf { CompactDimens }
