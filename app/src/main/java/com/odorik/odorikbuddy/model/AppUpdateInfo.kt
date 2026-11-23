package com.odorik.odorikbuddy.model

import com.google.gson.annotations.SerializedName

/**
 * The update feed (`app_version.json`). Every field has a default so Gson uses the
 * generated no-arg constructor and missing fields keep these values. `message_cs` /
 * `message_en` are optional additions; older app versions read `message` only.
 */
data class AppUpdateInfo(
    @SerializedName("version") val version: String = "",
    @SerializedName("download_url") val downloadUrl: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("message_cs") val messageCs: String? = null,
    @SerializedName("message_en") val messageEn: String? = null
) {
    /** Message in the app's language ("cs" / "en"); falls back to [message]. */
    fun localizedMessage(language: String): String {
        val specific = if (language == "cs") messageCs else messageEn
        return specific?.takeIf { it.isNotBlank() } ?: message
    }

    /**
     * [downloadUrl] only when it is an https link with a host, else null. The feed is a
     * hand-edited file, so never hand an arbitrary scheme (intent:, file:, …) to the system.
     */
    fun safeDownloadUrl(): String? {
        val url = downloadUrl.trim()
        return try {
            val uri = java.net.URI(url)
            if (uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()) url else null
        } catch (e: Exception) {
            null
        }
    }
}
