package com.odorik.odorikbuddy.data.model

import com.google.gson.reflect.TypeToken
import com.odorik.odorikbuddy.util.OdorikJsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class ActiveCallLegsTest {

    private val type = object : TypeToken<List<ActiveCall>>() {}.type

    private fun parse(body: String): List<ActiveCall> = OdorikJsonParser.parse<List<ActiveCall>>(body, type).getOrThrow()

    /** Real incoming call to a public number forwarded to an IP phone (2026-11-22, numbers anonymised). */
    private val incoming = """[
        {"id":213906128,"source_number":"00420600000000","destination_number":"00420315000000","destination_name":"Česko v síti- presmerovano","start_date":"2026-11-22T15:04:20Z","answer_date":"2026-11-22T15:04:23Z","price_per_minute":0.0,"line":760490},
        {"id":213906129,"source_number":"00420600000000","destination_number":"*760490","destination_name":"Česká rep. - * v síti","start_date":"2026-11-22T15:04:20Z","answer_date":"2026-11-22T15:04:23Z","price_per_minute":0.0,"line":760490}
    ]"""

    @Test
    fun `legs of one incoming call collapse to the public number leg`() {
        val calls = parse(incoming).collapseLegs()
        assertEquals(1, calls.size)
        assertEquals(213906128L, calls.single().id)
    }

    @Test
    fun `order of legs does not matter`() {
        assertEquals(213906128L, parse(incoming).reversed().collapseLegs().single().id)
    }

    @Test
    fun `different calls stay separate`() {
        val two = """[
            {"id":1,"source_number":"00420600000000","destination_number":"00420800123456","start_date":"2026-11-22T15:00:00Z"},
            {"id":2,"source_number":"00420700000000","destination_number":"00420800123456","start_date":"2026-11-22T15:00:00Z"}
        ]"""
        assertEquals(2, parse(two).collapseLegs().size)
    }

    @Test
    fun `calls without source or start never merge`() {
        assertEquals(2, parse("""[{"id":1},{"id":2}]""").collapseLegs().size)
    }
}
