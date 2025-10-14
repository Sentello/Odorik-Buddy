package com.odorik.odorikbuddy.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferences @Inject constructor(@ApplicationContext context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUser(user: String) {
        sharedPreferences.edit().putString("user", user).commit()
    }

    fun getUser(): String? {
        return sharedPreferences.getString("user", null)
    }

    fun savePassword(password: String) {
        sharedPreferences.edit().putString("password", password).commit()
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

    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).commit()
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun clearString(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}