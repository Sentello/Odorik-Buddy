package com.odorik.odorikbuddy.util

import com.odorik.odorikbuddy.BuildConfig

object VersionUtils {
    /**
     * Compares the current app version with the latest available version.
     * Returns true if latestVersion is higher than currentVersion.
     */
    fun isNewUpdateAvailable(latestVersion: String): Boolean {
        val currentVersion = BuildConfig.VERSION_NAME
        
        return try {
            val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
            val latestParts = latestVersion.split(".").map { it.toIntOrNull() ?: 0 }

            for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
                val current = currentParts.getOrNull(i) ?: 0
                val latest = latestParts.getOrNull(i) ?: 0
                if (latest > current) return true
                if (latest < current) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
