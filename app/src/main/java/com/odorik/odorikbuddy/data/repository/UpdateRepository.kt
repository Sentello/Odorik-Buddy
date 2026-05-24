package com.odorik.odorikbuddy.data.repository

import android.content.Context
import com.google.gson.Gson
import com.odorik.odorikbuddy.data.api.UpdateApi
import com.odorik.odorikbuddy.model.AppUpdateInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UpdateRepository @Inject constructor(
    private val updateApi: UpdateApi,
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "update_prefs"
        private const val KEY_CACHED_UPDATE_INFO = "cached_update_info"
    }

    suspend fun getAppUpdateInfo(): Result<AppUpdateInfo> {
        return try {
            val response = updateApi.getAppUpdateInfo()
            if (response.isSuccessful) {
                response.body()?.let {

                    cacheUpdateInfo(it)
                    Result.success(it)
                } ?: Result.failure(Exception("No data received"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCachedUpdateInfo(): AppUpdateInfo? {
        val json = prefs.getString(KEY_CACHED_UPDATE_INFO, null)
        return json?.let {
            try {
                gson.fromJson(it, AppUpdateInfo::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun cacheUpdateInfo(updateInfo: AppUpdateInfo) {
        val json = gson.toJson(updateInfo)
        prefs.edit().putString(KEY_CACHED_UPDATE_INFO, json).apply()
    }
}