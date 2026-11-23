package com.odorik.odorikbuddy.data.backup

import com.google.gson.annotations.SerializedName

/*
 * The settings backup file (JSON). Every field is nullable so files from older or newer
 * versions still parse; BackupCodec validates them.
 *
 * Every field has @SerializedName on purpose: release builds are minified, and without
 * it R8 would rename the fields and the JSON keys with them (see proguard-rules.pro).
 *
 * Never add the password here. See IMPLEMENTATION_PLAN_SETTINGS_BACKUP.md, decision 1.
 *
 * Compatibility rules (backups already sent between phones must keep importing):
 * - Adding a field: make it nullable with a null default and skip it on import when null.
 *   Older backups simply don't have it; FORMAT_VERSION stays the same.
 * - Never rename or remove a field, or change its type or meaning, within a format version.
 *   If that's needed, bump BackupCodec.FORMAT_VERSION, convert the old shape in
 *   BackupCodec.decode, and add a frozen backup-vN.json test next to BackupCompatibilityTest.
 * - appVersion is informational only; never branch on it.
 * BackupCompatibilityTest (frozen backup-v1.json) fails if a format-1 field stops importing.
 */

data class SettingsBackup(
    /** File marker, must be [BackupCodec.APP_MARKER]. */
    @SerializedName("app") val app: String? = null,
    @SerializedName("formatVersion") val formatVersion: Int? = null,
    /** ISO-8601 UTC, e.g. 2026-11-21T21:45:00Z. */
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("appVersion") val appVersion: String? = null,
    @SerializedName("account") val account: BackupAccount? = null,
    @SerializedName("display") val display: BackupDisplay? = null,
    @SerializedName("app_settings") val appSettings: BackupAppSettings? = null,
    @SerializedName("calls") val calls: BackupCalls? = null,
    @SerializedName("tiles") val tiles: List<BackupTile>? = null
)

/** For information only: shown in the import preview, never used to log in. */
data class BackupAccount(
    @SerializedName("username") val username: String? = null
)

data class BackupDisplay(
    @SerializedName("themeMode") val themeMode: String? = null,
    @SerializedName("appTheme") val appTheme: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("tileSize") val tileSize: String? = null,
    @SerializedName("navIconStyle") val navIconStyle: String? = null
)

data class BackupAppSettings(
    @SerializedName("historyPeriodDays") val historyPeriodDays: Int? = null,
    @SerializedName("autoUpdateEnabled") val autoUpdateEnabled: Boolean? = null,
    @SerializedName("directCallsEnabled") val directCallsEnabled: Boolean? = null
)

data class BackupCalls(
    @SerializedName("personalPhoneNumber") val personalPhoneNumber: String? = null,
    @SerializedName("callerId") val callerId: String? = null,
    @SerializedName("callbackRecipient") val callbackRecipient: String? = null,
    @SerializedName("oneShotRecipient") val oneShotRecipient: String? = null,
    @SerializedName("selectedLine") val selectedLine: String? = null,
    @SerializedName("useCallerIdPrefix") val useCallerIdPrefix: Boolean? = null,
    @SerializedName("tabOrder") val tabOrder: String? = null
)

data class BackupTile(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("position") val position: Int? = null,
    @SerializedName("label") val label: String? = null,
    @SerializedName("recipient") val recipient: String? = null,
    /** TileCallType name: CALLBACK or ONESHOT. */
    @SerializedName("callType") val callType: String? = null,
    @SerializedName("lineId") val lineId: String? = null,
    @SerializedName("callerId") val callerId: String? = null,
    @SerializedName("useLineAsCallerId") val useLineAsCallerId: Boolean? = null,
    /** ARGB as a Long (can exceed Int.MAX_VALUE). */
    @SerializedName("color") val color: Long? = null,
    @SerializedName("textColor") val textColor: Long? = null
)
