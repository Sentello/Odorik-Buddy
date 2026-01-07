package com.odorik.odorikbuddy.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun getResponsivePadding(): PaddingValues {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val responsiveHorizontalPadding = when {
        screenWidthDp < 320 -> 8.dp 
        screenWidthDp < 360 -> 12.dp 
        screenWidthDp < 412 -> 16.dp 
        else -> 24.dp 
    }

    val responsiveVerticalPadding = when {
        screenWidthDp < 360 -> 4.dp
        screenWidthDp < 412 -> 8.dp
        else -> 12.dp
    }

    return PaddingValues(
        start = responsiveHorizontalPadding,
        top = responsiveVerticalPadding,
        end = responsiveHorizontalPadding,
        bottom = responsiveVerticalPadding
    )
}

@Composable
fun getResponsiveSpacing(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    return when {
        screenWidthDp < 360 -> 12.dp
        screenWidthDp < 412 -> 16.dp
        else -> 24.dp
    }
}

@Composable
fun getResponsiveCardPadding(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    return when {
        screenWidthDp < 360 -> 12.dp
        screenWidthDp < 412 -> 16.dp
        else -> 20.dp
    }
}

@Composable
fun getResponsiveChartHeight(maxSpending: Double): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val baseHeight = if (screenWidthDp < 412) 180.dp else 200.dp
    val additionalHeight = (maxSpending * 3).coerceAtMost(80.0).dp

    return baseHeight + additionalHeight
}

@Composable
fun getResponsiveHeadlineLargeSize(): androidx.compose.ui.unit.TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    return when {
        screenWidthDp < 360 -> 28.sp
        screenWidthDp < 412 -> 30.sp
        else -> 32.sp
    }
}

@Composable
fun getResponsiveTitleLargeSize(): androidx.compose.ui.unit.TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    return when {
        screenWidthDp < 360 -> 20.sp
        screenWidthDp < 412 -> 22.sp
        else -> 24.sp
    }
}

@Composable
fun getResponsiveBodyLargeSize(): androidx.compose.ui.unit.TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    return when {
        screenWidthDp < 360 -> 14.sp
        screenWidthDp < 412 -> 15.sp
        else -> 16.sp
    }
}

@Composable
fun getResponsiveMessageFieldHeight(): Dp {  
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp

    return when {
        screenHeightDp < 680 -> 200.dp  
        screenHeightDp < 840 -> 360.dp  
        screenHeightDp < 1000 -> 400.dp 
        else -> 440.dp                  
    }
}

@Composable
fun getResponsiveMinMessageHeight(): Dp {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp

    return when {
        screenHeightDp < 680 -> 120.dp  
        screenHeightDp < 840 -> 240.dp  
        screenHeightDp < 1000 -> 320.dp 
        else -> 360.dp                  
    }
}

@Composable
fun getResponsiveMinLines(): Int {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    if (screenHeightDp < 680) {
        return 4
    } else if (screenHeightDp < 740) {
        return 8
    } else if (screenHeightDp < 840) {
        return 9
    } else if (screenHeightDp < 1000) {
        return 12
    } else {
        return 15
    }
}

@Composable
fun getResponsiveMaxLines(): Int {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    return when {
        screenHeightDp < 600 -> 4
        screenHeightDp < 700 -> 6
        screenHeightDp < 900 -> 12
        else -> 16
    }
}

@Composable
fun shouldShowNavigationLabels(): Boolean {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    
    return screenWidthDp >= 400
}

@Composable
fun getResponsiveNavigationLabelSize(): androidx.compose.ui.unit.TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    return when {
        screenWidthDp < 412 -> 12.sp 
        else -> 14.sp 
    }
}