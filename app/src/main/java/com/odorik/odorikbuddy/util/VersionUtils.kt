package com.odorik.odorikbuddy.util

import com.odorik.odorikbuddy.BuildConfig

object VersionUtils {


    fun isNewUpdateAvailable(latestVersion: String): Boolean =
        isNewer(latestVersion, BuildConfig.VERSION_NAME)


    fun isNewer(latest: String, current: String): Boolean = compare(latest, current) > 0


    fun compare(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.trim().toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.trim().toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val a = parts1.getOrNull(i) ?: 0
            val b = parts2.getOrNull(i) ?: 0
            if (a != b) return a.compareTo(b)
        }
        return 0
    }
}
