package com.odorik.odorikbuddy.data.repository

import android.content.Context
import com.google.gson.Gson
import com.odorik.odorikbuddy.data.api.UpdateApi
import com.odorik.odorikbuddy.model.AppUpdateInfo
import com.odorik.odorikbuddy.util.VersionUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
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
        private const val KEY_BANNER_DISMISSED_VERSION = "banner_dismissed_version"
    }

    suspend fun getAppUpdateInfo(): Result<AppUpdateInfo> {
        return try {
            val response = updateApi.getAppUpdateInfo()
            if (response.isSuccessful) {
                response.body()?.let {
                    // Cache the successful response
                    cacheUpdateInfo(it)
                    Result.success(it)
                } ?: Result.failure(Exception("No data received"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: CancellationException) {
            throw e
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

    /**
     * Newer version from the cached feed that the Dashboard banner should show, or null.
     * Reads the cache only — the Dashboard never fetches the feed itself.
     */
    fun getBannerUpdate(): AppUpdateInfo? {
        val info = getCachedUpdateInfo() ?: return null
        if (!VersionUtils.isNewUpdateAvailable(info.version)) return null
        if (prefs.getString(KEY_BANNER_DISMISSED_VERSION, null) == info.version) return null
        return info
    }

    /** The user hid the banner for [version]; a later version shows it again. */
    fun dismissBanner(version: String) {
        prefs.edit().putString(KEY_BANNER_DISMISSED_VERSION, version).apply()
    }

    private fun cacheUpdateInfo(updateInfo: AppUpdateInfo) {
        val json = gson.toJson(updateInfo)
        prefs.edit().putString(KEY_CACHED_UPDATE_INFO, json).apply()
    }
}