package com.odorik.odorikbuddy.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {

    private fun tile(id: Int, recipient: String?, label: String? = "Tile $id") = BackupTile(
        id = id,
        position = id,
        label = label,
        recipient = recipient,
        callType = "CALLBACK",
        lineId = "12345",
        callerId = "601000000",
        useLineAsCallerId = false,
        color = 0xFF4CAF50,
        textColor = null
    )

    private val full = SettingsBackup(
        app = BackupCodec.APP_MARKER,
        formatVersion = BackupCodec.FORMAT_VERSION,
        createdAt = "2026-11-21T21:45:00Z",
        appVersion = "1.5.6",
        account = BackupAccount(username = "123456"),
        display = BackupDisplay(themeMode = "DARK", appTheme = "ODORIK", language = "cs", tileSize = "LARGE"),
        appSettings = BackupAppSettings(historyPeriodDays = 30, autoUpdateEnabled = true, directCallsEnabled = false),
        calls = BackupCalls(
            personalPhoneNumber = "+420777123456",
            callerId = "601000000",
            callbackRecipient = "00359894896033",
            oneShotRecipient = "",
            selectedLine = "12345",
            useCallerIdPrefix = true,
            tabOrder = "tiles_title,callback_title,oneshot_call"
        ),
        tiles = listOf(tile(1, "+420777123456"), tile(2, "605123456"))
    )

    private fun ok(json: String): SettingsBackup {
        val result = BackupCodec.decode(json)
        assertTrue("expected Ok but was $result", result is DecodeResult.Ok)
        return (result as DecodeResult.Ok).backup
    }

    @Test
    fun roundTripKeepsEverything() {
        assertEquals(full, ok(BackupCodec.encode(full)))
    }

    @Test
    fun garbageIsNotABackup() {
        listOf("hello", "", "{}", "[]", "null", "{\"app\":\"Other\",\"formatVersion\":1}", "{\"app\":\"OdorikBuddy\"}")
            .forEach { assertEquals("input: $it", DecodeResult.NotABackup, BackupCodec.decode(it)) }
    }

    @Test
    fun newerFormatIsRefused() {
        val json = BackupCodec.encode(full.copy(formatVersion = BackupCodec.FORMAT_VERSION + 1))
        assertEquals(DecodeResult.NewerVersion, BackupCodec.decode(json))
    }

    @Test
    fun missingSectionsAreNull() {
        val backup = ok("""{"app":"OdorikBuddy","formatVersion":1,"tiles":[]}""")
        assertNull(backup.display)
        assertNull(backup.calls)
        assertNull(backup.appSettings)
        assertEquals(emptyList<BackupTile>(), backup.tiles)
    }

    @Test
    fun invalidTilesAreDroppedOthersKept() {
        val json = BackupCodec.encode(
            full.copy(tiles = listOf(tile(1, " "), tile(2, "605123456"), tile(3, "777", label = null)))
        )
        assertEquals(listOf(2), ok(json).tiles!!.map { it.id })
    }

    @Test
    fun nullTileElementIsDropped() {
        val backup = ok("""{"app":"OdorikBuddy","formatVersion":1,"tiles":[null,{"label":"A","recipient":"123"}]}""")
        assertEquals(1, backup.tiles!!.size)
    }

    @Test
    fun unknownFieldsAreIgnored() {
        val backup = ok("""{"app":"OdorikBuddy","formatVersion":1,"futureField":1,"display":{"language":"cs","x":true}}""")
        assertEquals("cs", backup.display?.language)
    }

    @Test
    fun encodedFileNeverContainsAPassword() {
        assertFalse(BackupCodec.encode(full).contains("password", ignoreCase = true))
    }
}
