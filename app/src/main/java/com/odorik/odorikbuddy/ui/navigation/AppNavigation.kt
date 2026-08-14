package com.odorik.odorikbuddy.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.odorik.odorikbuddy.ui.login.LoginScreen
import com.odorik.odorikbuddy.ui.main.MainScreen
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.LOGIN
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.MAIN
import com.odorik.odorikbuddy.ui.theme.Motion

@Composable
fun AppNavigation(
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = viewModel.getStartDestination(),
        enterTransition = {
            fadeIn(tween(Motion.durMedium, delayMillis = Motion.durShort)) +
                scaleIn(initialScale = 0.92f, animationSpec = tween(Motion.durMedium, delayMillis = Motion.durShort))
        },
        exitTransition = { fadeOut(tween(Motion.durShort)) },
        popEnterTransition = {
            fadeIn(tween(Motion.durMedium, delayMillis = Motion.durShort)) +
                scaleIn(initialScale = 0.92f, animationSpec = tween(Motion.durMedium, delayMillis = Motion.durShort))
        },
        popExitTransition = { fadeOut(tween(Motion.durShort)) }
    ) {
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