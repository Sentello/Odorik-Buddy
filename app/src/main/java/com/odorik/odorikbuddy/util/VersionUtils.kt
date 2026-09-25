package com.odorik.odorikbuddy.util

import com.odorik.odorikbuddy.BuildConfig

object VersionUtils {


    fun isNewUpdateAvailable(latestVersion: String): Boolean =
        isNewer(latestVersion, BuildConfig.VERSION_NAME)


    fun isNewer(latest: String, current: String): Boolean = compare(latest, current) > 0


    fun compare(v1: String, v2: String): Int {
        val parts1 = segments(v1)
        val parts2 = segments(v2)
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val a = parts1.getOrNull(i) ?: 0
            val b = parts2.getOrNull(i) ?: 0
            if (a != b) return a.compareTo(b)
        }
        return 0
    }


    private fun segments(version: String): List<Int> =
        version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .takeWhile { it.isDigit() || it == '.' }
            .split(".")
            .map { it.toIntOrNull() ?: 0 }
}
