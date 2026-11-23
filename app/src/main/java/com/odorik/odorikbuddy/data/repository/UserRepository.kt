package com.odorik.odorikbuddy.data.repository

import android.app.Application
import com.odorik.odorikbuddy.data.local.SecurePreferences
import javax.inject.Inject

/**
 * Credential store with two layers:
 * - **Session** (in-memory): used for auth and for "Remember me" = off (cleared on process death).
 * - **Persisted** (EncryptedSharedPreferences): used only when the user opts into Remember me.
 *
 * Login must set session credentials first, authenticate, then persist only on success.
 */
class UserRepository @Inject constructor(
    private val application: Application,
    private val securePreferences: SecurePreferences
) {

    @Volatile
    private var sessionUserId: String? = null

    @Volatile
    private var sessionPassword: String? = null

    /**
     * In-memory credentials for the current process. Does not touch encrypted storage.
     */
    fun setSessionCredentials(userId: String, password: String) {
        sessionUserId = userId
        sessionPassword = password
    }

    /**
     * Persist credentials to encrypted storage (Remember me = on).
     */
    fun persistCredentials(userId: String, password: String) {
        securePreferences.saveUser(userId)
        securePreferences.savePassword(password)
    }

    /**
     * Clear only encrypted storage, keeping in-memory session (session-only login).
     */
    fun clearPersistedCredentials() {
        securePreferences.clearUser()
        securePreferences.clearPassword()
    }

    fun clearSessionCredentials() {
        sessionUserId = null
        sessionPassword = null
    }

    /**
     * Clear both session and persisted credentials (logout / failed login).
     */
    fun clearCredentials() {
        clearSessionCredentials()
        clearPersistedCredentials()
    }

    /**
     * @deprecated Prefer [setSessionCredentials] + [persistCredentials] / [clearPersistedCredentials]
     * for correct login lifecycle. Kept for any residual call sites.
     */
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
