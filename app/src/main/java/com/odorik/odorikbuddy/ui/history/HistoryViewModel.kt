package com.odorik.odorikbuddy.ui.history

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
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history

    init {
        fetchHistory()
    }

    private fun fetchHistory() {
        viewModelScope.launch {
            val user = securePreferences.getUser()
            val password = securePreferences.getPassword()

            if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
                
                
                return@launch
            }

            
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            val now = Calendar.getInstance()
            val to = isoFormat.format(now.time)
            now.add(Calendar.DAY_OF_YEAR, -30)
            val from = isoFormat.format(now.time)

            try {
                val result = repository.getCombinedHistory(user, password, from, to)
                _history.value = result
            } catch (e: Exception) {
                
                e.printStackTrace()
            }
        }
    }
}
