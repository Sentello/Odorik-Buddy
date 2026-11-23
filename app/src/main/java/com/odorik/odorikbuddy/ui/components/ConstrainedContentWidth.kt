package com.odorik.odorikbuddy.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens

/**
 * Fills the available width; on Medium/Expanded windows additionally caps content
 * at [com.odorik.odorikbuddy.ui.theme.AppDimens.contentMaxWidth] and centers it.
 *
 * Self-contained on purpose: `fillMaxWidth` must precede `wrapContentWidth`, which must
 * precede `widthIn` — a call-site `fillMaxWidth()` before `widthIn` pins the min width
 * constraint and silently disables the cap.
 */
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
