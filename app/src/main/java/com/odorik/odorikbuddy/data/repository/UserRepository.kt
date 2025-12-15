package com.odorik.odorikbuddy.data.repository

import android.app.Application
import javax.inject.Inject
import com.odorik.odorikbuddy.data.local.SecurePreferences // Import SecurePreferences

class UserRepository @Inject constructor(
    private val application: Application,
    private val securePreferences: SecurePreferences // Inject SecurePreferences
) {

    fun saveCredentials(userId: String, password: String, remember: Boolean) {
        if (remember) {
            securePreferences.saveUser(userId)
            securePreferences.savePassword(password)
        } else {
            securePreferences.clearUser()
            securePreferences.clearPassword()
        }
    }

    fun clearCredentials() {
        securePreferences.clearUser()
        securePreferences.clearPassword()
    }

    fun getUserId(): String? {
        return securePreferences.getUser()
    }

    fun getPassword(): String? {
        return securePreferences.getPassword()
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != null && getPassword() != null
    }
}