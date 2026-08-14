package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.model.HistoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryRepositoryTest {

    private fun item(id: String, date: String, length: Int? = null) =
        HistoryItem(id = id, date = date, length = length)

    @Test
    fun `mergeAndTag tags by source endpoint and sorts newest first`() {
        val calls = listOf(item("c1", "2026-07-15T10:00:00+02:00", length = 30))
        val sms = listOf(
            item("s1", "2026-07-16T10:00:00+02:00"),
            item("s2", "2026-07-14T10:00:00+02:00")
        )

        val merged = HistoryRepository.mergeAndTag(calls, sms)

        assertEquals(listOf("s1", "c1", "s2"), merged.map { it.id })
        assertTrue(merged.first { it.id == "c1" }.isCall)
        assertTrue(merged.first { it.id == "s1" }.isSms)
    }

    @Test
    fun `endpoint tag wins over length inference`() {

        val merged = HistoryRepository.mergeAndTag(
            calls = listOf(item("c1", "2026-07-15T10:00:00+02:00", length = null)),
            sms = emptyList()
        )
        assertTrue(merged.single().isCall)
    }

    @Test
    fun `untagged cached item falls back to length inference`() {
        assertTrue(item("x", "2026-07-15T10:00:00+02:00", length = 10).isCall)
        assertTrue(item("y", "2026-07-15T10:00:00+02:00", length = null).isSms)
    }
}
