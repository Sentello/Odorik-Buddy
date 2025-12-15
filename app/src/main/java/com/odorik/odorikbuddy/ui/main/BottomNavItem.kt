package com.odorik.odorikbuddy.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.ui.graphics.vector.ImageVector
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.CALLS
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.DASHBOARD
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.HISTORY
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.SETTINGS
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.SMS

sealed class BottomNavItem(val route: String, val icon: ImageVector, val titleRes: Int) {
    object Dashboard : BottomNavItem(DASHBOARD, Icons.Default.Dashboard, R.string.dashboard)
    object Calls : BottomNavItem(CALLS, Icons.Default.Call, R.string.calls)
    object Sms : BottomNavItem(SMS, Icons.Default.Sms, R.string.sms)
    object History : BottomNavItem(HISTORY, Icons.Default.History, R.string.history)
    object Settings : BottomNavItem(SETTINGS, Icons.Default.Settings, R.string.settings)
}