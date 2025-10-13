package com.odorik.odorikbuddy.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Dashboard : BottomNavItem("dashboard", Icons.Default.Dashboard, "Dashboard")
    object Calls : BottomNavItem("calls", Icons.Default.Call, "Calls")
    object Sms : BottomNavItem("sms", Icons.Default.Sms, "Sms")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
}