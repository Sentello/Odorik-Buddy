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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.CALLS
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.DASHBOARD
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.HISTORY
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.SETTINGS
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.SMS
import com.odorik.odorikbuddy.ui.theme.CallAccent
import com.odorik.odorikbuddy.ui.theme.DashboardAccent
import com.odorik.odorikbuddy.ui.theme.HistoryAccent
import com.odorik.odorikbuddy.ui.theme.SettingsAccent
import com.odorik.odorikbuddy.ui.theme.SmsAccent

sealed class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val titleRes: Int,
    val accentColor: Color
) {
    object Dashboard : BottomNavItem(DASHBOARD, Icons.Filled.Dashboard, Icons.Outlined.Dashboard, R.string.dashboard, DashboardAccent)
    object Calls : BottomNavItem(CALLS, Icons.Filled.Call, Icons.Outlined.Call, R.string.calls, CallAccent)
    object Sms : BottomNavItem(SMS, Icons.Filled.Sms, Icons.Outlined.Sms, R.string.sms, SmsAccent)
    object History : BottomNavItem(HISTORY, Icons.Filled.History, Icons.Outlined.History, R.string.history, HistoryAccent)
    object Settings : BottomNavItem(SETTINGS, Icons.Filled.Settings, Icons.Outlined.Settings, R.string.settings, SettingsAccent)
}