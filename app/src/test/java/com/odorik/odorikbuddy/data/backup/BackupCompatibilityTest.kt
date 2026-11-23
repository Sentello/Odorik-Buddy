package com.odorik.odorikbuddy.data.backup

import com.odorik.odorikbuddy.data.local.entity.TileCallType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Backups already in users' hands must keep importing.
 *
 * `src/test/resources/backup/backup-v1.json` is a frozen format-1 backup (same structure as
 * a real 1.5.5 export, made-up data). NEVER edit it to make this test pass: if it fails,
 * a field was renamed or removed and old backups would silently lose that value.
 * When the format changes, add backup-v2.json and a new test next to this one.
 */
class BackupCompatibilityTest {

    private fun loadFixture(name: String): String =
        requireNotNull(javaClass.classLoader?.getResource("backup/$name")) { "missing fixture $name" }
            .readText(Charsets.UTF_8)

    private fun decodeV1(): SettingsBackup {
        val result = BackupCodec.decode(loadFixture("backup-v1.json"))
        assertTrue("format-1 backup must import, was $result", result is DecodeResult.Ok)
        return (result as DecodeResult.Ok).backup
    }

    @Test
    fun formatOneHeaderAndAccount() {
        val backup = decodeV1()
        assertEquals(1, backup.formatVersion)
        assertEquals("1.5.5", backup.appVersion)
        assertEquals("2026-11-21T20:06:36.358Z", backup.createdAt)
        assertEquals("1234567", backup.account?.username)
    }

    @Test
    fun formatOneDisplayAndAppSettings() {
        val backup = decodeV1()
        assertEquals(BackupDisplay(themeMode = "DARK", appTheme = "MATERIAL_YOU", language = "cs", tileSize = "LARGE"), backup.display)
        assertEquals(BackupAppSettings(historyPeriodDays = 30, autoUpdateEnabled = true, directCallsEnabled = false), backup.appSettings)
    }

    @Test
    fun formatOneCalls() {
        assertEquals(
            BackupCalls(
                personalPhoneNumber = "00420777000333",
                callerId = "601000000",
                callbackRecipient = "00420777000111",
                oneShotRecipient = "00420777000222",
                selectedLine = "123456",
                useCallerIdPrefix = true,
                tabOrder = "tiles_title,callback_title,oneshot_call"
            ),
            decodeV1().calls
        )
    }

    @Test
    fun formatOneTilesBecomeTheSameDatabaseRows() {
        val tiles = BackupTileMapper.toEntities(decodeV1().tiles.orEmpty())
        assertEquals(2, tiles.size)

        val grandma = tiles[0]
        assertEquals(1, grandma.id)
        assertEquals(0, grandma.position)
        assertEquals("Babička", grandma.label)
        assertEquals("777000444", grandma.recipient)
        assertEquals(TileCallType.CALLBACK, grandma.callType)
        assertEquals("123456", grandma.lineId)
        assertEquals("+420777000333", grandma.callerId)
        assertEquals(false, grandma.useLineAsCallerId)
        assertEquals(4292933626L, grandma.color)
        assertEquals(null, grandma.textColor)

        val grandpa = tiles[1]
        assertEquals(4, grandpa.id)
        assertEquals(1, grandpa.position)
        assertEquals("Děda", grandpa.label)
        assertEquals("+420777000555", grandpa.recipient)
        assertEquals(TileCallType.ONESHOT, grandpa.callType)
        assertEquals(null, grandpa.lineId)
        assertEquals(true, grandpa.useLineAsCallerId)
        assertEquals(4294967295L, grandpa.textColor)
    }

    /** Every key a 1.5.5 export writes must still be read. Catches a rename the other tests miss. */
    @Test
    fun everyFormatOneKeyIsStillKnown() {
        val reencoded = BackupCodec.encode(decodeV1())
        val keyPattern = Regex("\"([A-Za-z_]+)\"\\s*:")
        val fixtureKeys = keyPattern.findAll(loadFixture("backup-v1.json")).map { it.groupValues[1] }.toSet()
        val currentKeys = keyPattern.findAll(reencoded).map { it.groupValues[1] }.toSet()
        assertEquals(emptySet<String>(), fixtureKeys - currentKeys)
    }
}
