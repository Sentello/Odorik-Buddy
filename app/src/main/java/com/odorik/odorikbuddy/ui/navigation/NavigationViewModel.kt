package com.odorik.odorikbuddy.ui.navigation

import androidx.lifecycle.ViewModel
import com.odorik.odorikbuddy.data.repository.UserRepository
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.LOGIN
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.MAIN
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun getStartDestination(): String {
        return if (userRepository.isLoggedIn()) {
            MAIN
        } else {
            LOGIN
        }
    }
}