package com.odorik.odorikbuddy.util

import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OdorikJsonParserTest {

    private val stringListType = object : TypeToken<List<String>>() {}.type

    @Test
    fun `plain text error yields its code`() {
        assertEquals("unauthorized", OdorikJsonParser.errorCodeOf("error unauthorized"))
        assertEquals("missing_argument", OdorikJsonParser.errorCodeOf("error missing_argument caller"))
    }

    @Test
    fun `json errors array yields the first code`() {
        assertEquals(
            "invalid_number",
            OdorikJsonParser.errorCodeOf("""{"errors":["invalid_number","name_too_long"]}""")
        )
    }

    @Test
    fun `non error bodies have no code`() {
        listOf("[]", "{}", "ok", "", """{"balance": 12.5}""").forEach {
            assertNull(it, OdorikJsonParser.errorCodeOf(it))
        }
        assertNull(OdorikJsonParser.errorCodeOf(null))
    }

    @Test
    fun `parse returns the value or an OdorikApiException`() {
        val ok = OdorikJsonParser.parse<List<String>>("""["a","b"]""", stringListType)
        assertEquals(listOf("a", "b"), ok.getOrNull())

        val empty = OdorikJsonParser.parse<List<String>>("[]", stringListType)
        assertEquals(emptyList<String>(), empty.getOrNull())

        val err = OdorikJsonParser.parse<List<String>>("error unauthorized", stringListType)
        assertEquals("unauthorized", (err.exceptionOrNull() as OdorikApiException).code)
    }

    @Test
    fun `parse of an empty body fails`() {
        assertTrue(OdorikJsonParser.parse<List<String>>("", stringListType).isFailure)
    }

    @Test
    fun `ack accepts ok and empty object, rejects errors`() {
        assertTrue(OdorikJsonParser.parseAck("{}").isSuccess)
        assertTrue(OdorikJsonParser.parseAck("ok").isSuccess)
        assertTrue(OdorikJsonParser.parseAck("").isSuccess)
        assertTrue(OdorikJsonParser.parseAck("""{"errors":["unauthorized"]}""").isFailure)
    }
}
