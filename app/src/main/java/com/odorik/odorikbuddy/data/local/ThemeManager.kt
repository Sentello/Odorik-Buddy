package com.odorik.odorikbuddy.data.local

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val PREFS_NAME = "theme_prefs"
    private val KEY_DARK_MODE = "dark_mode"

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    
    val isDarkMode: MutableState<Boolean> = mutableStateOf(sharedPreferences.getBoolean(KEY_DARK_MODE, false))

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        isDarkMode.value = enabled 
    }
}
