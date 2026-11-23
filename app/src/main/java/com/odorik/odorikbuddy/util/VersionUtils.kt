package com.odorik.odorikbuddy.util

import com.odorik.odorikbuddy.BuildConfig

object VersionUtils {

    /** True when [latestVersion] is strictly newer than the installed app version. */
    fun isNewUpdateAvailable(latestVersion: String): Boolean =
        isNewer(latestVersion, BuildConfig.VERSION_NAME)

    /** True when [latest] is a strictly higher dotted version than [current]. */
    fun isNewer(latest: String, current: String): Boolean = compare(latest, current) > 0

    /**
     * Compares dotted numeric versions ("1.4.4" vs "1.10"). Missing or
     * non-numeric segments count as 0. Returns <0, 0, or >0.
     */
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

    /**
     * Splits a version into numeric segments, tolerating the decorations that show up in
     * release tags: "v1.4.4" and "1.4.4-beta2" both parse to [1, 4, 4].
     *
     * This matters because the update feed is a hand-maintained JSON file. Without the
     * prefix strip, "v1.5.1".split(".") yields "v1" -> 0, so a newer release would compare
     * as OLDER than the installed version and update notifications would stop silently.
     */
    private fun segments(version: String): List<Int> =
        version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .takeWhile { it.isDigit() || it == '.' }
            .split(".")
            .map { it.toIntOrNull() ?: 0 }
}
