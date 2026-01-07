package com.odorik.odorikbuddy.ui.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.odorik.odorikbuddy.ui.calls.CallScreen
import com.odorik.odorikbuddy.ui.dashboard.DashboardScreen
import com.odorik.odorikbuddy.ui.dashboard.DateRangePickerScreen
import com.odorik.odorikbuddy.ui.history.HistoryScreen
import com.odorik.odorikbuddy.ui.navigation.SettingsRoutes
import com.odorik.odorikbuddy.ui.routes.OwnNumbersScreen
import com.odorik.odorikbuddy.ui.routes.RoutesScreen
import com.odorik.odorikbuddy.ui.settings.RoutingOptionsScreen
import com.odorik.odorikbuddy.ui.settings.SettingsScreen
import com.odorik.odorikbuddy.ui.sms.SmsScreen
import com.odorik.odorikbuddy.util.getResponsiveNavigationLabelSize
import com.odorik.odorikbuddy.util.shouldShowNavigationLabels

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val items = listOf(
                    BottomNavItem.Dashboard,
                    BottomNavItem.Calls,
                    BottomNavItem.Sms,
                    BottomNavItem.History,
                    BottomNavItem.Settings
                )
                items.forEach { screen ->
                    val label = stringResource(screen.titleRes)
                    val showLabels = shouldShowNavigationLabels()
                    val labelFontSize = getResponsiveNavigationLabelSize()
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = label 
                            )
                        },
                        label = if (showLabels) {
                            {
                                Text(
                                    text = label,
                                    fontSize = labelFontSize,
                                    maxLines = 1
                                )
                            }
                        } else null,
                        selected = when (screen) {
                            is BottomNavItem.Settings -> {
                                currentDestination?.route?.startsWith(BottomNavItem.Settings.route) == true
                            }
                            else -> {
                                currentDestination?.route == screen.route
                            }
                        },
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.semantics {
                            role = Role.Tab
                            contentDescription = "Navigate to $label tab"
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            bottomNavController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) { DashboardScreen(navController = bottomNavController) }
            composable(BottomNavItem.Calls.route) { CallScreen() }
            composable(BottomNavItem.Sms.route) { SmsScreen() }
            composable(BottomNavItem.History.route) { HistoryScreen() }
            composable("date_range_picker") { DateRangePickerScreen(navController = bottomNavController) }
            navigation(startDestination = SettingsRoutes.SETTINGS_HOME, route = BottomNavItem.Settings.route) {
                composable(SettingsRoutes.SETTINGS_HOME) {
                    SettingsScreen(outerNavController = navController, internalNavController = bottomNavController)
                }
                composable(SettingsRoutes.ROUTES_SCREEN) { RoutesScreen(internalNavController = bottomNavController) }
                composable(SettingsRoutes.OWN_NUMBERS_SCREEN) { OwnNumbersScreen(internalNavController = bottomNavController) }
                composable(SettingsRoutes.ROUTING_OPTIONS_SCREEN) {
                    RoutingOptionsScreen(internalNavController = bottomNavController)
                }
            }
        }
    }
}
