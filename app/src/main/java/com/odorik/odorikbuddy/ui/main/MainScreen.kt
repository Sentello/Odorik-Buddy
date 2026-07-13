package com.odorik.odorikbuddy.ui.main

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
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
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes
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
    val viewModel: MainViewModel = hiltViewModel()
    val bottomNavController = rememberNavController()

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
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
                    val isSelected = when (screen) {
                        BottomNavItem.Dashboard -> {
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true ||
                                    currentDestination?.route == com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.DATE_RANGE_PICKER
                        }
                        else -> {
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        }
                    }

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.25f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "IconScale"
                    )

                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = label, // Accessibility: Spoken label for icon
                                modifier = Modifier.scale(scale)
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
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = screen.accentColor.copy(alpha = 0.2f),
                            selectedIconColor = screen.accentColor,
                            selectedTextColor = screen.accentColor
                        ),
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                val tabDestination = currentDestination?.hierarchy?.firstOrNull { it.route == screen.route }
                                if (tabDestination is androidx.navigation.NavGraph) {
                                    bottomNavController.popBackStack(tabDestination.startDestinationId, inclusive = false)
                                } else if (currentDestination?.route == NavigationRoutes.DATE_RANGE_PICKER && screen.route == BottomNavItem.Dashboard.route) {
                                    bottomNavController.popBackStack(BottomNavItem.Dashboard.route, inclusive = false)
                                }
                            } else {
                                bottomNavController.navigate(screen.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                viewModel.saveLastScreen(screen.route)
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
            startDestination = viewModel.getLastScreen(),
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) { DashboardScreen(navController = bottomNavController) }
            composable(BottomNavItem.Calls.route) { CallScreen() }
            composable(BottomNavItem.Sms.route) { SmsScreen() }
            composable(BottomNavItem.History.route) { HistoryScreen() }
            composable(NavigationRoutes.DATE_RANGE_PICKER) { DateRangePickerScreen(navController = bottomNavController) }
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
