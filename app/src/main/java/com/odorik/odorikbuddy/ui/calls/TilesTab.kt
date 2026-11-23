package com.odorik.odorikbuddy.ui.calls

import androidx.compose.runtime.Composable

// Shared helper functions
@Composable
fun TilesTab(
    viewModel: CallViewModel
) {
    TilesScreen(
        callViewModel = viewModel
    )
}
