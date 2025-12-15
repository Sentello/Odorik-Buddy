package com.odorik.odorikbuddy.ui.history

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.repository.HistoryRepository
import com.odorik.odorikbuddy.model.HistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import com.odorik.odorikbuddy.data.local.SecurePreferences

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository,
    private val securePreferences: SecurePreferences,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    data class HistoryDisplayItem(
        val item: HistoryItem,
        val isChild: Boolean = false
    )
    
    private val _history = MutableStateFlow<List<HistoryDisplayItem>>(emptyList())
    val history: StateFlow<List<HistoryDisplayItem>> = _history

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchHistory()
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null // Clear previous errors on a new fetch attempt
            val user = securePreferences.getUser()
            val password = securePreferences.getPassword()

            if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
                _error.value = "User not logged in or credentials missing"
                _isRefreshing.value = false
                return@launch
            }

            // Fetch history for the user-selected period (default 90 days)
            val days = sharedPreferences.getInt("history_period_days", 90)
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            val now = Calendar.getInstance()
            val to = isoFormat.format(now.time)
            now.add(Calendar.DAY_OF_YEAR, -days)
            val from = isoFormat.format(now.time)

            try {
                val result = repository.getCombinedHistory(user, password, from, to)
                val grouped = result.groupBy { it.redirection_parent_id }
                val parents = grouped[null] ?: emptyList()
                val displayItems = mutableListOf<HistoryDisplayItem>()
                parents.sortedByDescending { it.date }.forEach { parent ->
                    displayItems.add(HistoryDisplayItem(parent, false))
                    val children = grouped[parent.id] ?: emptyList()
                    children.sortedByDescending { it.date }.forEach { child ->
                        displayItems.add(HistoryDisplayItem(child, true))
                    }
                }
                // Add any standalone items not grouped (though unlikely)
                val standalones = result.filter { it.redirection_parent_id == null && !parents.contains(it) }
                standalones.sortedByDescending { it.date }.forEach { standalone ->
                    displayItems.add(HistoryDisplayItem(standalone, false))
                }
                _history.value = displayItems
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred."
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
