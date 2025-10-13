package com.odorik.odorikbuddy

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.odorik.odorikbuddy.ui.login.LoginScreen
import com.odorik.odorikbuddy.ui.main.MainScreen
import com.odorik.odorikbuddy.data.repository.UserRepository

import com.odorik.odorikbuddy.ui.sms.SmsScreen

@Composable
fun AppNavigation(navController: NavHostController, userRepository: UserRepository) {
    val startDestination = if (userRepository.getUserId() != null) "main" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(onLoginSuccess = { 
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("main") {
            MainScreen(navController = navController)
        }
        composable("sms_screen") {
            SmsScreen()
        }
    }
}