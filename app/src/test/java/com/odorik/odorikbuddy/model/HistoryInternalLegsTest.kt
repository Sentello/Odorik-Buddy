package com.odorik.odorikbuddy.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryInternalLegsTest {

    private fun call(id: String, parent: String?, direction: String, dest: String) =
        HistoryItem(id = id, direction = direction, sourceNumber = "00420700000000", destinationNumber = dest, redirectionParentId = parent)

    /** Real shape (2026-11-22): incoming call to the public number + leg ringing the own line. */
    private val incoming = call("825549157", null, "in", "00420315315657")
    private val internalLeg = call("825549158", "825549157", "redirected", "*760490")

    @Test
    fun `internal leg to own line is hidden, parent stays`() {
        assertTrue(internalLeg.isInternalLineLeg)
        assertFalse(incoming.isInternalLineLeg)
        assertEquals(listOf(incoming), listOf(incoming, internalLeg).withoutInternalLineLegs())
    }

    @Test
    fun `real forwarding to another number stays`() {
        val toMobile = call("2", "825549157", "redirected", "00420736000000")
        assertEquals(listOf(incoming, toMobile), listOf(incoming, toMobile).withoutInternalLineLegs())
    }

    private fun lineCall(id: String, direction: String, date: String, src: String = "*760490", dest: String = "*760490") =
        HistoryItem(id = id, date = date, direction = direction, sourceNumber = src, destinationNumber = dest, length = 0)

    /** Real records (2026-11-22): one call between own lines logged as "out" and "in", 1 s apart. */
    @Test
    fun `line to line call shows once, the out record stays`() {
        val inRec = lineCall("825553604", "in", "2026-11-22T15:50:37Z")
        val outRec = lineCall("825553603", "out", "2026-11-22T15:50:38Z")
        assertEquals(listOf(outRec), listOf(inRec, outRec).withoutInternalLineLegs())
    }

    @Test
    fun `line to line calls minutes apart are both kept`() {
        val inRec = lineCall("1", "in", "2026-11-22T15:00:00Z")
        val outRec = lineCall("2", "out", "2026-11-22T15:50:38Z")
        assertEquals(2, listOf(inRec, outRec).withoutInternalLineLegs().size)
    }

    @Test
    fun `incoming call from outside to a line is never treated as a mirror`() {
        val fromOutside = HistoryItem(id = "5", date = "2026-11-22T15:50:38Z", direction = "in",
            sourceNumber = "00420700000000", destinationNumber = "*760490", length = 10)
        val outRec = lineCall("6", "out", "2026-11-22T15:50:38Z")
        assertEquals(2, listOf(fromOutside, outRec).withoutInternalLineLegs().size)
    }

    @Test
    fun `a call the user dials to a star code is not a leg`() {
        val dialed = call("3", null, "out", "*100")
        assertFalse(dialed.isInternalLineLeg)
    }
}
