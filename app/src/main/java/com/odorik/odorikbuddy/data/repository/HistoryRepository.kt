package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.local.HistoryDao
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.model.HistoryItem
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject


class HistoryRepository @Inject constructor(
    private val apiService: OdorikApi,
    private val historyDao: HistoryDao,
    private val userRepository: UserRepository
) {


    suspend fun getCombinedHistory(from: String, to: String): List<HistoryItem> = coroutineScope {
        val (user, pass) = userRepository.requireCredentials()


        val callsDeferred = async { apiService.getCallHistory(user, pass, from, to) }
        val smsDeferred = async { apiService.getSmsHistory(user, pass, from, to) }

        val callsResponse = callsDeferred.await()
        val smsResponse = smsDeferred.await()

        val calls = if (callsResponse.isSuccessful) callsResponse.body() ?: emptyList() else throw Exception(callsResponse.errorBody()?.string() ?: "Failed to fetch call history")
        val sms = if (smsResponse.isSuccessful) smsResponse.body() ?: emptyList() else throw Exception(smsResponse.errorBody()?.string() ?: "Failed to fetch sms history")

        mergeAndTag(calls, sms)
    }

    companion object {

        fun mergeAndTag(calls: List<HistoryItem>, sms: List<HistoryItem>): List<HistoryItem> =
            (calls.map { it.copy(eventTypeRaw = "call") } + sms.map { it.copy(eventTypeRaw = "sms") })
                .sortedByDescending { it.date }
    }


    suspend fun insertHistory(items: List<HistoryItem>) {
        historyDao.insertHistory(items)
    }


    suspend fun getCachedHistory(): List<HistoryItem> {
        return historyDao.getAllHistory()
    }


    suspend fun pruneHistoryBefore(minIsoDate: String) {
        historyDao.deleteOlderThan(minIsoDate)
    }
}
