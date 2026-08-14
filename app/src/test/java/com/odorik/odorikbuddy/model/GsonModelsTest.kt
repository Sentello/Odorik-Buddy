package com.odorik.odorikbuddy.model

import com.google.gson.Gson
import com.odorik.odorikbuddy.data.model.Line
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GsonModelsTest {

    private val gson = Gson()

    @Test
    fun `call history item deserializes and classifies as call`() {
        val json = """
            {"id":"c1","date":"2026-07-17T10:00:00+02:00","direction":"out",
             "source_number":"00420777123456","destination_number":"00420608123456",
             "length":65,"ringing_length":5,"price":1.5,"price_per_minute":1.4,
             "status":"answered","line":101}
        """.trimIndent()
        val item = gson.fromJson(json, HistoryItem::class.java)
        assertEquals("c1", item.id)
        assertEquals(65, item.length)
        assertEquals(1.5, item.price, 0.0)
        assertEquals(101, item.line)
        assertTrue(item.isCall)
        assertFalse(item.isSms)
        assertTrue(item.isOutgoing)
    }

    @Test
    fun `sms history item has null length and classifies as sms`() {
        val json = """
            {"id":"s1","date":"2026-07-17T11:00:00+02:00","direction":"in",
             "source_number":"00420608123456","destination_number":"00420777123456",
             "price":0.9}
        """.trimIndent()
        val item = gson.fromJson(json, HistoryItem::class.java)
        assertNull(item.length)
        assertTrue(item.isSms)
        assertFalse(item.isCall)
        assertTrue(item.isIncoming)
    }

    @Test
    fun `line deserializes with snake_case fields`() {
        val json = """
            {"id":101,"name":"Main line","caller_id":"00420777123456",
             "public_number":"00420910123456","sip_password":"secret",
             "connected_devices":[]}
        """.trimIndent()
        val line = gson.fromJson(json, Line::class.java)
        assertEquals(101, line.id)
        assertEquals("Main line", line.name)
        assertEquals("00420777123456", line.callerId)
        assertEquals("00420910123456", line.publicNumber)
        assertEquals("secret", line.sipPassword)
        assertTrue(line.connectedDevices.isEmpty())
    }
}
