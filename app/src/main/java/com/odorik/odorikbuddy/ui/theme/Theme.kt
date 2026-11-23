package com.odorik.odorikbuddy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.window.core.layout.WindowWidthSizeClass
import com.odorik.odorikbuddy.data.local.AppTheme
import com.odorik.odorikbuddy.data.local.NavIconStyle
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.local.ThemeMode

val LocalIsAppDark = staticCompositionLocalOf { false }
val LocalAppTheme = staticCompositionLocalOf { AppTheme.STANDARD }
val LocalNavIconStyle = staticCompositionLocalOf { NavIconStyle.CLASSIC }

// Every role is set on purpose: a role left out would be filled from
// Material's default (purple) baseline and show up on chips and toggles.
private val StandardLightScheme = lightColorScheme(
    primary = StandardPrimaryLight,
    onPrimary = StandardOnPrimaryLight,
    primaryContainer = StandardPrimaryContainerLight,
    onPrimaryContainer = StandardOnPrimaryContainerLight,
    inversePrimary = StandardInversePrimaryLight,
    secondary = StandardSecondaryLight,
    secondaryContainer = StandardSecondaryContainerLight,
    onSecondaryContainer = StandardOnSecondaryContainerLight,
    tertiary = StandardTertiaryLight,
    tertiaryContainer = StandardTertiaryContainerLight,
    onTertiaryContainer = StandardOnTertiaryContainerLight,
    background = StandardBackgroundLight,
    onBackground = StandardOnSurfaceLight,
    surface = StandardSurfaceLight,
    onSurface = StandardOnSurfaceLight,
    surfaceVariant = StandardSurfaceVariantLight,
    onSurfaceVariant = StandardOnSurfaceVariantLight,
    surfaceTint = StandardPrimaryLight,
    inverseSurface = StandardInverseSurfaceLight,
    inverseOnSurface = StandardInverseOnSurfaceLight,
    surfaceBright = StandardSurfaceLight,
    surfaceDim = StandardSurfaceDimLight,
    surfaceContainerLowest = StandardSurfaceContainerLowestLight,
    surfaceContainerLow = StandardSurfaceContainerLowLight,
    surfaceContainer = StandardSurfaceContainerLight,
    surfaceContainerHigh = StandardSurfaceContainerHighLight,
    surfaceContainerHighest = StandardSurfaceContainerHighestLight,
    outline = StandardOutlineLight,
    outlineVariant = StandardOutlineVariantLight
)

private val StandardDarkScheme = darkColorScheme(
    primary = StandardPrimaryDark,
    onPrimary = StandardOnPrimaryDark,
    primaryContainer = StandardPrimaryContainerDark,
    onPrimaryContainer = StandardOnPrimaryContainerDark,
    inversePrimary = StandardInversePrimaryDark,
    secondary = StandardSecondaryDark,
    onSecondary = StandardOnSecondaryDark,
    secondaryContainer = StandardSecondaryContainerDark,
    onSecondaryContainer = StandardOnSecondaryContainerDark,
    tertiary = StandardTertiaryDark,
    onTertiary = StandardOnTertiaryDark,
    tertiaryContainer = StandardTertiaryContainerDark,
    onTertiaryContainer = StandardOnTertiaryContainerDark,
    background = StandardBackgroundDark,
    onBackground = StandardOnSurfaceDark,
    surface = StandardSurfaceDark,
    onSurface = StandardOnSurfaceDark,
    surfaceVariant = StandardSurfaceVariantDark,
    onSurfaceVariant = StandardOnSurfaceVariantDark,
    surfaceTint = StandardPrimaryDark,
    inverseSurface = StandardInverseSurfaceDark,
    inverseOnSurface = StandardInverseOnSurfaceDark,
    surfaceBright = StandardSurfaceBrightDark,
    surfaceDim = StandardSurfaceDark,
    surfaceContainerLowest = StandardSurfaceContainerLowestDark,
    surfaceContainerLow = StandardSurfaceContainerLowDark,
    surfaceContainer = StandardSurfaceContainerDark,
    surfaceContainerHigh = StandardSurfaceContainerHighDark,
    surfaceContainerHighest = StandardSurfaceContainerHighestDark,
    outline = StandardOutlineDark,
    outlineVariant = StandardOutlineVariantDark
)

private val OdorikLightScheme = lightColorScheme(
    primary = OdorikPrimaryLight,
    onPrimary = OdorikOnPrimaryLight,
    primaryContainer = OdorikPrimaryContainerLight,
    onPrimaryContainer = OdorikOnPrimaryContainerLight,
    inversePrimary = OdorikInversePrimaryLight,
    secondary = OdorikSecondaryLight,
    secondaryContainer = OdorikSecondaryContainerLight,
    onSecondaryContainer = OdorikOnSecondaryContainerLight,
    tertiary = OdorikTertiaryLight,
    tertiaryContainer = OdorikTertiaryContainerLight,
    onTertiaryContainer = OdorikOnTertiaryContainerLight,
    background = OdorikBackgroundLight,
    onBackground = OdorikOnSurfaceLight,
    surface = OdorikSurfaceLight,
    onSurface = OdorikOnSurfaceLight,
    surfaceVariant = OdorikSurfaceVariantLight,
    onSurfaceVariant = OdorikOnSurfaceVariantLight,
    surfaceTint = OdorikPrimaryLight,
    inverseSurface = OdorikInverseSurfaceLight,
    inverseOnSurface = OdorikInverseOnSurfaceLight,
    surfaceBright = OdorikSurfaceLight,
    surfaceDim = OdorikSurfaceDimLight,
    surfaceContainerLowest = OdorikSurfaceContainerLowestLight,
    surfaceContainerLow = OdorikSurfaceContainerLowLight,
    surfaceContainer = OdorikSurfaceContainerLight,
    surfaceContainerHigh = OdorikSurfaceContainerHighLight,
    surfaceContainerHighest = OdorikSurfaceContainerHighestLight,
    outline = OdorikOutlineLight,
    outlineVariant = OdorikOutlineVariantLight
)

private val OdorikDarkScheme = darkColorScheme(
    primary = OdorikPrimaryDark,
    onPrimary = OdorikOnPrimaryDark,
    primaryContainer = OdorikPrimaryContainerDark,
    onPrimaryContainer = OdorikOnPrimaryContainerDark,
    inversePrimary = OdorikInversePrimaryDark,
    secondary = OdorikSecondaryDark,
    onSecondary = OdorikOnSecondaryDark,
    secondaryContainer = OdorikSecondaryContainerDark,
    onSecondaryContainer = OdorikOnSecondaryContainerDark,
    tertiary = OdorikTertiaryDark,
    onTertiary = OdorikOnTertiaryDark,
    tertiaryContainer = OdorikTertiaryContainerDark,
    onTertiaryContainer = OdorikOnTertiaryContainerDark,
    background = OdorikBackgroundDark,
    onBackground = OdorikOnSurfaceDark,
    surface = OdorikSurfaceDark,
    onSurface = OdorikOnSurfaceDark,
    surfaceVariant = OdorikSurfaceVariantDark,
    onSurfaceVariant = OdorikOnSurfaceVariantDark,
    surfaceTint = OdorikPrimaryDark,
    inverseSurface = OdorikInverseSurfaceDark,
    inverseOnSurface = OdorikInverseOnSurfaceDark,
    surfaceBright = OdorikSurfaceBrightDark,
    surfaceDim = OdorikSurfaceDark,
    surfaceContainerLowest = OdorikSurfaceContainerLowestDark,
    surfaceContainerLow = OdorikSurfaceContainerLowDark,
    surfaceContainer = OdorikSurfaceContainerDark,
    surfaceContainerHigh = OdorikSurfaceContainerHighDark,
    surfaceContainerHighest = OdorikSurfaceContainerHighestDark,
    outline = OdorikOutlineDark,
    outlineVariant = OdorikOutlineVariantDark
)

@Composable
fun OdorikBuddyTheme(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val themeMode by themeManager.themeMode
    val appTheme by themeManager.appTheme
    val navIconStyle by themeManager.navIconStyle
    val systemDark = isSystemInDarkTheme()

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when (appTheme) {
        AppTheme.MATERIAL_YOU -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) StandardDarkScheme else StandardLightScheme
            }
        }
        AppTheme.ODORIK -> if (darkTheme) OdorikDarkScheme else OdorikLightScheme
        AppTheme.STANDARD -> if (darkTheme) StandardDarkScheme else StandardLightScheme
    }

    val view = LocalView.current
    SideEffect {
        if (!view.isInEditMode) {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val dimens = when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.EXPANDED -> ExpandedDimens
        WindowWidthSizeClass.MEDIUM -> MediumDimens
        else -> CompactDimens
    }

    CompositionLocalProvider(
        LocalIsAppDark provides darkTheme,
        LocalAppTheme provides appTheme,
        LocalNavIconStyle provides navIconStyle,
        LocalAppDimens provides dimens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
