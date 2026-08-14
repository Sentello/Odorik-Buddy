package com.odorik.odorikbuddy.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: Dp = 72.dp,
    cornerRadius: Dp = 20.dp
) {
    val infinite = rememberInfiniteTransition(label = "skeleton")
    val alpha by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    )
}

@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp = 110.dp
) {
    SkeletonBox(modifier = modifier, height = height, cornerRadius = 20.dp)
}

@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        SkeletonCard(height = 110.dp)
        Spacer(modifier = Modifier.height(16.dp))
        SkeletonCard(height = 140.dp)
        Spacer(modifier = Modifier.height(16.dp))
        SkeletonCard(height = 220.dp)
    }
}

@Composable
fun HistoryListSkeleton(rowCount: Int = 5, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(rowCount) {
            SkeletonBox(height = 72.dp, cornerRadius = 16.dp)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
