package com.odorik.odorikbuddy.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences by lazy {
        context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
    }

    fun saveCredentials(user: String, password: String) {
        sharedPreferences.edit()
            .putString("user", user)
            .putString("password", password)
            .apply()
    }

    fun getSecureUser(): String? {
        return sharedPreferences.getString("user", null)
    }

    fun getSecurePassword(): String? {
        return sharedPreferences.getString("password", null)
    }
}
