package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.local.HistoryDao
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.model.HistoryItem
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Repository for fetching call and SMS history from the Odorik API.
 */
class HistoryRepository @Inject constructor(
    private val apiService: OdorikApi,
    private val historyDao: HistoryDao,
    private val userRepository: UserRepository
) {

    /**
     * Fetches both call and SMS history using centrally managed credentials.
     */
    suspend fun getCombinedHistory(from: String, to: String): List<HistoryItem> = coroutineScope {
        val (user, pass) = userRepository.requireCredentials()

        // Fetch calls and SMS in parallel
        val callsDeferred = async { apiService.getCallHistory(user, pass, from, to) }
        val smsDeferred = async { apiService.getSmsHistory(user, pass, from, to) }
        
        val callsResponse = callsDeferred.await()
        val smsResponse = smsDeferred.await()
        
        val calls = if (callsResponse.isSuccessful) callsResponse.body() ?: emptyList() else throw Exception(callsResponse.errorBody()?.string() ?: "Failed to fetch call history")
        val sms = if (smsResponse.isSuccessful) smsResponse.body() ?: emptyList() else throw Exception(smsResponse.errorBody()?.string() ?: "Failed to fetch sms history")
        
        // Combine the two lists and sort them with the newest items first.
        (calls + sms).sortedByDescending { it.date }
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
