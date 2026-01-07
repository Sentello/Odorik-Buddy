package com.odorik.odorikbuddy.ui.main

import androidx.lifecycle.ViewModel
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val LAST_SCREEN_KEY = "last_screen"

    fun getLastScreen(): String {
        return securePreferences.getString(LAST_SCREEN_KEY, NavigationRoutes.DASHBOARD) ?: NavigationRoutes.DASHBOARD
    }

    fun saveLastScreen(route: String) {
        securePreferences.saveString(LAST_SCREEN_KEY, route)
    }
}