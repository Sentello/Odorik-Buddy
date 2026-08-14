package com.odorik.odorikbuddy.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferences @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val PREFS_FILE = "secure_prefs"
        private const val TAG = "SecurePreferences"
    }


    private val sharedPreferences: SharedPreferences by lazy { createPreferences() }


    private fun createPreferences(): SharedPreferences {
        return try {
            create()
        } catch (e: Exception) {
            Log.w(TAG, "Encrypted prefs unreadable, resetting", e)
            context.deleteSharedPreferences(PREFS_FILE)
            create()
        }
    }

    private fun create(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFS_FILE,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveUser(user: String) {
        sharedPreferences.edit().putString("user", user).apply()
    }

    fun getUser(): String? {
        return sharedPreferences.getString("user", null)
    }

    fun savePassword(password: String) {
        sharedPreferences.edit().putString("password", password).apply()
    }

    fun getPassword(): String? {
        return sharedPreferences.getString("password", null)
    }

    fun clearUser() {
        sharedPreferences.edit().remove("user").apply()
    }

    fun clearPassword() {
        sharedPreferences.edit().remove("password").apply()
    }





    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun clearString(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}