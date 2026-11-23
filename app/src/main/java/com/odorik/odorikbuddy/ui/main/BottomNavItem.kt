package com.odorik.odorikbuddy.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.twotone.Call
import androidx.compose.material.icons.twotone.Dashboard
import androidx.compose.material.icons.twotone.History
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material.icons.twotone.Sms
import androidx.compose.ui.graphics.vector.ImageVector
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.NavIconStyle
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.CALLS
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.DASHBOARD
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.HISTORY
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.SETTINGS
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.SMS
import com.odorik.odorikbuddy.ui.theme.ScreenAccent
import com.odorik.odorikbuddy.ui.theme.ScreenAccents

/** The same symbol in each Material icon set the user can pick in Settings. */
class NavIconSet(
    val filled: ImageVector,
    val outlined: ImageVector,
    val rounded: ImageVector,
    val twoTone: ImageVector
) {
    fun icon(selected: Boolean, style: NavIconStyle): ImageVector = when (style) {
        NavIconStyle.CLASSIC -> if (selected) filled else outlined
        // Rounded has no outlined variant: selection shows through colour, indicator and scale.
        NavIconStyle.ROUNDED -> rounded
        NavIconStyle.TWO_TONE -> if (selected) filled else twoTone
    }
}

sealed class BottomNavItem(
    val route: String,
    val icons: NavIconSet,
    val titleRes: Int,
    val screenAccent: ScreenAccent
) {
    object Dashboard : BottomNavItem(
        DASHBOARD,
        NavIconSet(Icons.Filled.Dashboard, Icons.Outlined.Dashboard, Icons.Rounded.Dashboard, Icons.TwoTone.Dashboard),
        R.string.dashboard, ScreenAccents.Dashboard
    )
    object Calls : BottomNavItem(
        CALLS,
        NavIconSet(Icons.Filled.Call, Icons.Outlined.Call, Icons.Rounded.Call, Icons.TwoTone.Call),
        R.string.calls, ScreenAccents.Calls
    )
    object Sms : BottomNavItem(
        SMS,
        NavIconSet(Icons.Filled.Sms, Icons.Outlined.Sms, Icons.Rounded.Sms, Icons.TwoTone.Sms),
        R.string.sms, ScreenAccents.Sms
    )
    object History : BottomNavItem(
        HISTORY,
        NavIconSet(Icons.Filled.History, Icons.Outlined.History, Icons.Rounded.History, Icons.TwoTone.History),
        R.string.history, ScreenAccents.History
    )
    object Settings : BottomNavItem(
        SETTINGS,
        NavIconSet(Icons.Filled.Settings, Icons.Outlined.Settings, Icons.Rounded.Settings, Icons.TwoTone.Settings),
        R.string.settings, ScreenAccents.Settings
    )

    companion object {
        /** Tabs in bar order. */
        val all: List<BottomNavItem> get() = listOf(Dashboard, Calls, Sms, History, Settings)
    }
}
