package com.odorik.odorikbuddy.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens


fun Modifier.constrainedContentWidth(): Modifier = composed {
    val max = LocalAppDimens.current.contentMaxWidth
    if (max == Dp.Infinity) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = max)
    }
}
