package com.odorik.odorikbuddy.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppUpdateInfoTest {

    private val info = AppUpdateInfo(
        version = "1.6.0",
        downloadUrl = "https://forum.odorik.cz/viewtopic.php?p=1",
        message = "Obecná zpráva",
        messageCs = "Česky",
        messageEn = "English"
    )

    @Test
    fun `czech uses message_cs when set`() {
        assertEquals("Česky", info.localizedMessage("cs"))
        assertEquals("Obecná zpráva", info.copy(messageCs = null).localizedMessage("cs"))
    }

    @Test
    fun `english uses message_en when set`() {
        assertEquals("English", info.localizedMessage("en"))
        assertEquals("Obecná zpráva", info.copy(messageEn = null).localizedMessage("en"))
    }

    @Test
    fun `blank localized message falls back to message`() {
        assertEquals("Obecná zpráva", info.copy(messageEn = "  ").localizedMessage("en"))
    }

    @Test
    fun `only https links with a host are safe`() {
        assertEquals("https://forum.odorik.cz/viewtopic.php?p=1", info.safeDownloadUrl())
        assertEquals("https://a.cz/x", info.copy(downloadUrl = "  https://a.cz/x ").safeDownloadUrl())
        assertNull(info.copy(downloadUrl = "http://x.cz").safeDownloadUrl())
        assertNull(info.copy(downloadUrl = "intent://x").safeDownloadUrl())
        assertNull(info.copy(downloadUrl = "").safeDownloadUrl())
        assertNull(info.copy(downloadUrl = "https://").safeDownloadUrl())
        assertNull(info.copy(downloadUrl = "not a url").safeDownloadUrl())
    }

    @Test
    fun `old feed without localized fields parses`() {
        val parsed = Gson().fromJson(
            """{"version":"1.6.0","download_url":"https://a.cz","message":"x"}""",
            AppUpdateInfo::class.java
        )
        assertEquals("1.6.0", parsed.version)
        assertNull(parsed.messageCs)
        assertNull(parsed.messageEn)
        assertEquals("x", parsed.localizedMessage("en"))
    }

    @Test
    fun `new feed fills localized fields`() {
        val parsed = Gson().fromJson(
            """{"version":"1.6.0","download_url":"https://a.cz","message":"x",
               "message_cs":"cz","message_en":"en"}""",
            AppUpdateInfo::class.java
        )
        assertEquals("cz", parsed.localizedMessage("cs"))
        assertEquals("en", parsed.localizedMessage("en"))
    }
}
