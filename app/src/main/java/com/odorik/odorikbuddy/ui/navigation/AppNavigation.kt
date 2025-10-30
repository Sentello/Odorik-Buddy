package com.odorik.odorikbuddy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.odorik.odorikbuddy.ui.login.LoginScreen
import com.odorik.odorikbuddy.ui.main.MainScreen
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.LOGIN
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.MAIN

@Composable
fun AppNavigation(
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = viewModel.getStartDestination()) {
        composable(LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(MAIN) {
                    popUpTo(LOGIN) { inclusive = true }
                }
            })
        }
        composable(MAIN) {
            MainScreen(navController = navController)
        }
    }
}