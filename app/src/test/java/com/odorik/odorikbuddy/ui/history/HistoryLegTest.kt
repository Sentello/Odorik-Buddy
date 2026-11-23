package com.odorik.odorikbuddy.ui.history

import com.odorik.odorikbuddy.model.HistoryItem
import com.odorik.odorikbuddy.ui.history.HistoryViewModel.HistoryDisplayItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryLegTest {

    private fun call(id: String) = HistoryDisplayItem(HistoryItem(id = id, direction = "in"))
    private fun leg(id: String, parent: String) =
        HistoryDisplayItem(HistoryItem(id = id, direction = "redirected", redirectionParentId = parent), isChild = true)

    @Test
    fun `leg right under its call is drawn as a leg`() {
        assertTrue(isLegUnder(leg("2", parent = "1"), above = call("1")))
    }

    @Test
    fun `second leg of the same call is drawn as a leg too`() {
        assertTrue(isLegUnder(leg("3", parent = "1"), above = leg("2", parent = "1")))
    }

    @Test
    fun `leg without its call above gets a full row`() {
        // A number filter matched only the leg, or its call is outside the loaded period.
        assertFalse(isLegUnder(leg("2", parent = "1"), above = null))
        assertFalse(isLegUnder(leg("2", parent = "1"), above = call("9")))
        assertFalse(isLegUnder(leg("2", parent = "1"), above = leg("8", parent = "7")))
    }

    @Test
    fun `ordinary calls are never legs`() {
        assertFalse(isLegUnder(call("2"), above = call("1")))
    }
}
