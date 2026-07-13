package com.odorik.odorikbuddy.data.local

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(
    name = "app_prefs",
    produceMigrations = { context ->
        listOf(
            object : DataMigration<Preferences> {
                override suspend fun shouldMigrate(currentData: Preferences): Boolean {
                    val oldPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    return oldPrefs.all.isNotEmpty()
                }

                override suspend fun migrate(currentData: Preferences): Preferences {
                    // Migrate from old SharedPreferences
                    val oldPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    val mutablePrefs = currentData.toMutablePreferences()

                    // Migrate known keys
                    if (oldPrefs.contains("direct_calls_enabled")) {
                        mutablePrefs[booleanPreferencesKey("direct_calls_enabled")] = 
                            oldPrefs.getBoolean("direct_calls_enabled", false)
                    }
                    if (oldPrefs.contains("history_period_days")) {
                        mutablePrefs[intPreferencesKey("history_period_days")] = 
                            oldPrefs.getInt("history_period_days", 90)
                    }
                    if (oldPrefs.contains("phone_number")) {
                        oldPrefs.getString("phone_number", null)?.let {
                            mutablePrefs[stringPreferencesKey("phone_number")] = it
                        }
                    }
                    if (oldPrefs.contains("auto_update_enabled")) {
                        mutablePrefs[booleanPreferencesKey("auto_update_enabled")] = 
                            oldPrefs.getBoolean("auto_update_enabled", false)
                    }
                    // Add more keys here as needed during migration

                    // Clear old prefs after migration (optional but recommended)
                    oldPrefs.edit().clear().apply()

                    return mutablePrefs.toPreferences()
                }

                override suspend fun cleanUp() {}
            }
        )
    }
)

/**
 * Modern AppPreferences backed by Jetpack DataStore (Preferences).
 * Replaces the old SharedPreferences-based implementation.
 */
@Singleton
class AppPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore = context.dataStore

    // --- Direct call setting ---
    var directCallsEnabled: Boolean
        get() = runBlocking { getBoolean(KEY_DIRECT_CALLS_ENABLED, false) }
        set(value) { putBoolean(KEY_DIRECT_CALLS_ENABLED, value) }

    // --- Auto update setting ---
    var autoUpdateEnabled: Boolean
        get() = runBlocking { getBoolean(KEY_AUTO_UPDATE_ENABLED, false) }
        set(value) { putBoolean(KEY_AUTO_UPDATE_ENABLED, value) }

    // --- History filter ---
    var historyPeriodDays: Int
        get() = runBlocking { getInt(KEY_HISTORY_PERIOD_DAYS, 90) }
        set(value) { putInt(KEY_HISTORY_PERIOD_DAYS, value) }

    // --- Generic access ---
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return dataStore.data.first()[booleanPreferencesKey(key)] ?: defaultValue
    }

    fun putBoolean(key: String, value: Boolean) {
        runBlocking {
            dataStore.edit { it[booleanPreferencesKey(key)] = value }
        }
    }

    suspend fun getInt(key: String, defaultValue: Int = 0): Int {
        return dataStore.data.first()[intPreferencesKey(key)] ?: defaultValue
    }

    fun putInt(key: String, value: Int) {
        runBlocking {
            dataStore.edit { it[intPreferencesKey(key)] = value }
        }
    }

    suspend fun getString(key: String, defaultValue: String? = null): String? {
        return dataStore.data.first()[stringPreferencesKey(key)] ?: defaultValue
    }

    fun putString(key: String, value: String) {
        runBlocking {
            dataStore.edit { it[stringPreferencesKey(key)] = value }
        }
    }

    companion object {
        const val KEY_DIRECT_CALLS_ENABLED = "direct_calls_enabled"
        const val KEY_HISTORY_PERIOD_DAYS = "history_period_days"
        const val KEY_AUTO_UPDATE_ENABLED = "auto_update_enabled"
    }
}
