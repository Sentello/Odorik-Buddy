package com.odorik.odorikbuddy.data.backup

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException

/** Result of reading a backup file's text. */
sealed interface DecodeResult {
    data class Ok(val backup: SettingsBackup) : DecodeResult
    /** Not JSON, or not an OdorikBuddy backup (marker or version missing). */
    data object NotABackup : DecodeResult
    /** Made by a newer app version with a format this version doesn't know. */
    data object NewerVersion : DecodeResult
}

/** JSON encoding and validation of [SettingsBackup]. No Android dependencies (unit-tested). */
object BackupCodec {
    const val APP_MARKER = "OdorikBuddy"
    const val FORMAT_VERSION = 1

    /** Refuse absurd files; a real backup with 200 tiles is about 60 KB. */
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

        // One broken tile must not fail the whole import: drop it, keep the rest.
        val validTiles = backup.tiles.orEmpty().filter { tile ->
            // Gson can put a null element into the list for `[null]`.
            @Suppress("SENSELESS_COMPARISON")
            tile != null && tile.label != null && !tile.recipient.isNullOrBlank()
        }
        return DecodeResult.Ok(backup.copy(tiles = validTiles))
    }
}
