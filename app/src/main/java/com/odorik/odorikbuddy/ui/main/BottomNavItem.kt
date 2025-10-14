package com.odorik.odorikbuddy.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.odorik.odorikbuddy.R

sealed class BottomNavItem(val route: String, val icon: ImageVector, val titleRes: Int) {
    object Dashboard : BottomNavItem("dashboard", Icons.Default.Dashboard, R.string.dashboard)
    object Calls : BottomNavItem("calls", Icons.Default.Call, R.string.calls)
    object Sms : BottomNavItem("sms", Icons.Default.Sms, R.string.sms)
    object Settings : BottomNavItem("settings", Icons.Default.Settings, R.string.settings)
}