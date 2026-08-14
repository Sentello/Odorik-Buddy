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
import androidx.compose.ui.graphics.vector.ImageVector
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.CALLS
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.DASHBOARD
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.HISTORY
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.SETTINGS
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.SMS
import com.odorik.odorikbuddy.ui.theme.ScreenAccent
import com.odorik.odorikbuddy.ui.theme.ScreenAccents

sealed class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val titleRes: Int,
    val screenAccent: ScreenAccent
) {
    object Dashboard : BottomNavItem(
        DASHBOARD, Icons.Filled.Dashboard, Icons.Outlined.Dashboard,
        R.string.dashboard, ScreenAccents.Dashboard
    )
    object Calls : BottomNavItem(
        CALLS, Icons.Filled.Call, Icons.Outlined.Call,
        R.string.calls, ScreenAccents.Calls
    )
    object Sms : BottomNavItem(
        SMS, Icons.Filled.Sms, Icons.Outlined.Sms,
        R.string.sms, ScreenAccents.Sms
    )
    object History : BottomNavItem(
        HISTORY, Icons.Filled.History, Icons.Outlined.History,
        R.string.history, ScreenAccents.History
    )
    object Settings : BottomNavItem(
        SETTINGS, Icons.Filled.Settings, Icons.Outlined.Settings,
        R.string.settings, ScreenAccents.Settings
    )
}
