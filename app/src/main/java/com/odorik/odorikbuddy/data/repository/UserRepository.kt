package com.odorik.odorikbuddy.data.repository

import android.app.Application
import com.odorik.odorikbuddy.data.local.SecurePreferences
import javax.inject.Inject


class UserRepository @Inject constructor(
    private val application: Application,
    private val securePreferences: SecurePreferences
) {

    @Volatile
    private var sessionUserId: String? = null

    @Volatile
    private var sessionPassword: String? = null


    fun setSessionCredentials(userId: String, password: String) {
        sessionUserId = userId
        sessionPassword = password
    }


    fun persistCredentials(userId: String, password: String) {
        securePreferences.saveUser(userId)
        securePreferences.savePassword(password)
    }


    fun clearPersistedCredentials() {
        securePreferences.clearUser()
        securePreferences.clearPassword()
    }

    fun clearSessionCredentials() {
        sessionUserId = null
        sessionPassword = null
    }


    fun clearCredentials() {
        clearSessionCredentials()
        clearPersistedCredentials()
    }


    fun saveCredentials(userId: String, password: String, remember: Boolean) {
        setSessionCredentials(userId, password)
        if (remember) {
            persistCredentials(userId, password)
        } else {
            clearPersistedCredentials()
        }
    }

    fun getUserId(): String? {
        return sessionUserId ?: securePreferences.getUser()
    }

    fun getPassword(): String? {
        return sessionPassword ?: securePreferences.getPassword()
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != null && getPassword() != null
    }

    fun requireCredentials(): Pair<String, String> {
        val userId = getUserId() ?: throw CredentialsNotSetException()
        val password = getPassword() ?: throw CredentialsNotSetException()
        return userId to password
    }
}

class CredentialsNotSetException : Exception("User credentials are not set")
