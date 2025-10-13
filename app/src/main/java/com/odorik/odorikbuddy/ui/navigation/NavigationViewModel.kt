package com.odorik.odorikbuddy.ui.navigation

import androidx.lifecycle.ViewModel
import com.odorik.odorikbuddy.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun getStartDestination(): String {
        return if (userRepository.isLoggedIn()) {
            "main"
        } else {
            "login"
        }
    }
}