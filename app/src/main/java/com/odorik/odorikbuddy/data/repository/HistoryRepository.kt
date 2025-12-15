package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.local.HistoryDao
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.model.HistoryItem
import javax.inject.Inject


class HistoryRepository @Inject constructor(
    private val apiService: OdorikApi,
    private val historyDao: HistoryDao
) {

    
    suspend fun getCombinedHistory(user: String, pass: String, from: String, to: String): List<HistoryItem> {
        
        val calls = apiService.getCallHistory(user, pass, from, to)
        val sms = apiService.getSmsHistory(user, pass, from, to)
        
        
        return (calls + sms).sortedByDescending { it.date }
    }

    
    suspend fun insertHistory(items: List<HistoryItem>) {
        historyDao.insertHistory(items)
    }

    
    suspend fun getCachedHistory(): List<HistoryItem> {
        return historyDao.getAllHistory()
    }
}
