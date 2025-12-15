package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.local.HistoryDao
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.model.HistoryItem
import javax.inject.Inject

/**
 * Repository for fetching call and SMS history from the Odorik API.
 */
class HistoryRepository @Inject constructor(
    private val apiService: OdorikApi,
    private val historyDao: HistoryDao
) {

    /**
     * Fetches both call and SMS history, combines them, and sorts them by date.
     */
    suspend fun getCombinedHistory(user: String, pass: String, from: String, to: String): List<HistoryItem> {
        // Fetch calls and SMS in parallel
        val calls = apiService.getCallHistory(user, pass, from, to)
        val sms = apiService.getSmsHistory(user, pass, from, to)
        
        // Combine the two lists and sort them with the newest items first.
        return (calls + sms).sortedByDescending { it.date }
    }

    /**
     * Inserts history items into local database for offline caching.
     */
    suspend fun insertHistory(items: List<HistoryItem>) {
        historyDao.insertHistory(items)
    }

    /**
     * Retrieves cached history from local database for offline use.
     */
    suspend fun getCachedHistory(): List<HistoryItem> {
        return historyDao.getAllHistory()
    }
}
