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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
                    val oldPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    val mutablePrefs = currentData.toMutablePreferences()

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

                    oldPrefs.edit().clear().apply()
                    return mutablePrefs.toPreferences()
                }

                override suspend fun cleanUp() {}
            }
        )
    }
)


@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePreferences: SecurePreferences
) {

    private val dataStore = context.dataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val loadMutex = Mutex()

    @Volatile private var loaded = false
    @Volatile private var _directCallsEnabled = false
    @Volatile private var _autoUpdateEnabled = false
    @Volatile private var _historyPeriodDays = 90
    @Volatile private var stringValues: Map<String, String> = emptyMap()

    init {


        scope.launch {
            ensureLoadedSuspend()
            dataStore.data.collect { prefs ->
                applyFrom(prefs)
            }
        }
    }

    private fun applyFrom(prefs: Preferences) {
        _directCallsEnabled = prefs[booleanPreferencesKey(KEY_DIRECT_CALLS_ENABLED)] ?: false
        _autoUpdateEnabled = prefs[booleanPreferencesKey(KEY_AUTO_UPDATE_ENABLED)] ?: false
        _historyPeriodDays = prefs[intPreferencesKey(KEY_HISTORY_PERIOD_DAYS)] ?: 90
        stringValues = prefs.asMap().entries
            .mapNotNull { (key, value) -> (value as? String)?.let { key.name to it } }
            .toMap()
    }


    private fun ensureLoaded() {
        if (loaded) return
        runBlocking(Dispatchers.IO) {
            ensureLoadedSuspend()
        }
    }

    var directCallsEnabled: Boolean
        get() {
            ensureLoaded()
            return _directCallsEnabled
        }
        set(value) {
            _directCallsEnabled = value
            loaded = true
            scope.launch {
                dataStore.edit { it[booleanPreferencesKey(KEY_DIRECT_CALLS_ENABLED)] = value }
            }
        }

    var autoUpdateEnabled: Boolean
        get() {
            ensureLoaded()
            return _autoUpdateEnabled
        }
        set(value) {
            _autoUpdateEnabled = value
            loaded = true
            scope.launch {
                dataStore.edit { it[booleanPreferencesKey(KEY_AUTO_UPDATE_ENABLED)] = value }
            }
        }

    var historyPeriodDays: Int
        get() {
            ensureLoaded()
            return _historyPeriodDays
        }
        set(value) {
            _historyPeriodDays = value
            loaded = true
            scope.launch {
                dataStore.edit { it[intPreferencesKey(KEY_HISTORY_PERIOD_DAYS)] = value }
            }
        }


    fun getString(key: String, defaultValue: String? = null): String? {
        ensureLoaded()
        return stringValues[key] ?: defaultValue
    }

    fun saveString(key: String, value: String) {

        stringValues = stringValues + (key to value)
        scope.launch {
            dataStore.edit { it[stringPreferencesKey(key)] = value }
        }
    }

    fun clearString(key: String) {
        stringValues = stringValues - key
        scope.launch {
            dataStore.edit { it.remove(stringPreferencesKey(key)) }
        }
    }


    suspend fun getHistoryPeriodDaysSuspend(): Int {
        ensureLoadedSuspend()
        return _historyPeriodDays
    }

    private suspend fun ensureLoadedSuspend() {
        if (loaded) return
        loadMutex.withLock {
            if (loaded) return
            migrateFromSecurePreferencesIfNeeded()
            applyFrom(dataStore.data.first())
            loaded = true
        }
    }


    private suspend fun migrateFromSecurePreferencesIfNeeded() {
        val migratedFlag = booleanPreferencesKey(KEY_SECURE_PREFS_MIGRATED)
        if (dataStore.data.first()[migratedFlag] == true) return

        val values = MIGRATED_KEYS.mapNotNull { key ->
            securePreferences.getString(key)?.let { key to it }
        }
        dataStore.edit { prefs ->
            values.forEach { (key, value) ->

                if (prefs[stringPreferencesKey(key)] == null) {
                    prefs[stringPreferencesKey(key)] = value
                }
            }
            prefs[migratedFlag] = true
        }
        MIGRATED_KEYS.forEach { securePreferences.clearString(it) }
    }

    companion object {
        const val KEY_DIRECT_CALLS_ENABLED = "direct_calls_enabled"
        const val KEY_HISTORY_PERIOD_DAYS = "history_period_days"
        const val KEY_AUTO_UPDATE_ENABLED = "auto_update_enabled"
        const val KEY_SECURE_PREFS_MIGRATED = "secure_prefs_migrated"


        val MIGRATED_KEYS = listOf(
            "phone_number", "caller_id", "recipient", "oneshot_recipient",
            "selected_line", "use_caller_id_prefix", "calls_selected_tab",
            "calls_tab_order", "dashboard_start_date", "dashboard_end_date",
            "last_screen"
        )
    }
}
