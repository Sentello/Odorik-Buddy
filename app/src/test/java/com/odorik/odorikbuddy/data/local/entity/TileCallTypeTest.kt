package com.odorik.odorikbuddy.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class TileCallTypeTest {

    private val converter = TileCallTypeConverter()

    @Test
    fun `stored values are the same strings as before the enum`() {
        assertEquals("CALLBACK", converter.toKey(TileCallType.CALLBACK))
        assertEquals("ONESHOT", converter.toKey(TileCallType.ONESHOT))
    }

    @Test
    fun `stored values are read back`() {
        assertEquals(TileCallType.CALLBACK, converter.fromKey("CALLBACK"))
        assertEquals(TileCallType.ONESHOT, converter.fromKey("ONESHOT"))
    }

    @Test
    fun `unknown value is a one-shot call, as it always was`() {
        assertEquals(TileCallType.ONESHOT, converter.fromKey("SOMETHING_ELSE"))
        assertEquals(TileCallType.ONESHOT, TileCallType.fromKey(null))
    }
}
