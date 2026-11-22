package com.odorik.odorikbuddy.data.backup

import com.google.gson.annotations.SerializedName


data class SettingsBackup(
    @SerializedName("app") val app: String? = null,
    @SerializedName("formatVersion") val formatVersion: Int? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("appVersion") val appVersion: String? = null,
    @SerializedName("account") val account: BackupAccount? = null,
    @SerializedName("display") val display: BackupDisplay? = null,
    @SerializedName("app_settings") val appSettings: BackupAppSettings? = null,
    @SerializedName("calls") val calls: BackupCalls? = null,
    @SerializedName("tiles") val tiles: List<BackupTile>? = null
)

data class BackupAccount(
    @SerializedName("username") val username: String? = null
)

data class BackupDisplay(
    @SerializedName("themeMode") val themeMode: String? = null,
    @SerializedName("appTheme") val appTheme: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("tileSize") val tileSize: String? = null
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
    @SerializedName("callType") val callType: String? = null,
    @SerializedName("lineId") val lineId: String? = null,
    @SerializedName("callerId") val callerId: String? = null,
    @SerializedName("useLineAsCallerId") val useLineAsCallerId: Boolean? = null,
    @SerializedName("color") val color: Long? = null,
    @SerializedName("textColor") val textColor: Long? = null
)
