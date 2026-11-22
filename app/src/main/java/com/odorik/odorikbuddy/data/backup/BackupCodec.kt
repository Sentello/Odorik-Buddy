package com.odorik.odorikbuddy.data.backup

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException

sealed interface DecodeResult {
    data class Ok(val backup: SettingsBackup) : DecodeResult
    data object NotABackup : DecodeResult
    data object NewerVersion : DecodeResult
}

object BackupCodec {
    const val APP_MARKER = "OdorikBuddy"
    const val FORMAT_VERSION = 1

    const val MAX_BYTES = 1_000_000

    private val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()

    fun encode(backup: SettingsBackup): String = gson.toJson(backup)

    fun decode(json: String): DecodeResult {
        val backup = try {
            gson.fromJson(json, SettingsBackup::class.java)
        } catch (e: JsonParseException) {
            null
        } catch (e: IllegalStateException) {
            null
        } catch (e: ClassCastException) {
            null
        } ?: return DecodeResult.NotABackup

        if (backup.app != APP_MARKER) return DecodeResult.NotABackup
        val version = backup.formatVersion ?: return DecodeResult.NotABackup
        if (version < 1) return DecodeResult.NotABackup
        if (version > FORMAT_VERSION) return DecodeResult.NewerVersion

        val validTiles = backup.tiles.orEmpty().filter { tile ->
            @Suppress("SENSELESS_COMPARISON")
            tile != null && tile.label != null && !tile.recipient.isNullOrBlank()
        }
        return DecodeResult.Ok(backup.copy(tiles = validTiles))
    }
}
