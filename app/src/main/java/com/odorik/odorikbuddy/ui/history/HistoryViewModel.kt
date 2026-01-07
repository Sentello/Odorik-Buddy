package com.odorik.odorikbuddy.ui.history

import android.content.ContentResolver
import android.content.SharedPreferences
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.repository.HistoryRepository
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.model.HistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository,
    private val securePreferences: SecurePreferences,
    private val sharedPreferences: SharedPreferences,
    private val getLinesUseCase: GetLinesUseCase
) : ViewModel() {

    data class HistoryDisplayItem(
        val item: HistoryItem,
        val isChild: Boolean = false
    )
    
    private val _history = MutableStateFlow<List<HistoryDisplayItem>>(emptyList())
    val history: StateFlow<List<HistoryDisplayItem>> = _history

    private val _filteredHistory = MutableStateFlow<List<HistoryDisplayItem>>(emptyList())
    val filteredHistory: StateFlow<List<HistoryDisplayItem>> = _filteredHistory

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _lines = MutableStateFlow<List<Line>>(emptyList())
    val lines: StateFlow<List<Line>> = _lines

    private val _selectedLine = MutableStateFlow<Line?>(null)
    val selectedLine: StateFlow<Line?> = _selectedLine

    private val _filterNumber = MutableStateFlow<String>("")
    val filterNumber: StateFlow<String> = _filterNumber

    private val _eventTypeFilter = MutableStateFlow<String>("all") 
    val eventTypeFilter: StateFlow<String> = _eventTypeFilter

    private val _eventDirectionFilter = MutableStateFlow<String>("all") 
    val eventDirectionFilter: StateFlow<String> = _eventDirectionFilter

    
    private val _contactsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val contactsMap: StateFlow<Map<String, String>> = _contactsMap.asStateFlow()

    init {
        viewModelScope.launch {
            
            fetchLines() 
            kotlinx.coroutines.delay(100) 
            fetchHistory() 
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null 
            val user = securePreferences.getUser()
            val password = securePreferences.getPassword()

            if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
                _error.value = "User not logged in or credentials missing"
                _isRefreshing.value = false
                return@launch
            }

            try {
                
                
                val days = sharedPreferences.getInt("history_period_days", 90)
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                isoFormat.timeZone = TimeZone.getTimeZone("UTC") 
                val now = Calendar.getInstance(TimeZone.getTimeZone("UTC")) 
                val to = isoFormat.format(now.time)
                now.add(Calendar.DAY_OF_YEAR, -days)
                val from = isoFormat.format(now.time)

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
                
                val standalones = result.filter { it.redirection_parent_id == null && !parents.contains(it) }
                standalones.sortedByDescending { it.date }.forEach { standalone ->
                    displayItems.add(HistoryDisplayItem(standalone, false))
                }
                _history.value = displayItems
                applyFilters()
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred."
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun fetchLines() {
        viewModelScope.launch {
            val result = getLinesUseCase.execute()
            result.onSuccess {
                _lines.value = it
                
                applyFilters()
            }.onFailure {
                _error.value = it.message ?: "Failed to fetch lines"
            }
        }
    }
    
    
    fun refreshData() {
        viewModelScope.launch {
            
            fetchLines()
            kotlinx.coroutines.delay(200) 
            fetchHistory()
        }
    }

    fun setSelectedLine(line: Line?) {
        _selectedLine.value = line
        applyFilters()
    }

    fun setFilterNumber(number: String) {
        _filterNumber.value = number
        applyFilters()
    }

    fun setEventTypeFilter(type: String) {
        _eventTypeFilter.value = type
        applyFilters()
    }

    fun setEventDirectionFilter(direction: String) {
        _eventDirectionFilter.value = direction
        applyFilters()
    }

    private fun applyFilters() {
        val lineFilter = _selectedLine.value
        val numberFilter = _filterNumber.value
        val eventTypeFilter = _eventTypeFilter.value
        val eventDirectionFilter = _eventDirectionFilter.value

        val filtered = if (lineFilter == null && numberFilter.isBlank() && eventTypeFilter == "all" && eventDirectionFilter == "all") {
            
            _history.value
        } else {
            _history.value.filter { displayItem ->
                val item = displayItem.item
                
                
                val lineMatch = lineFilter?.let {
                    item.line == it.id
                } ?: true
                
                
                val numberMatch = if (numberFilter.isBlank()) {
                    true
                } else {
                    item.source_number.contains(numberFilter) || item.destination_number.contains(numberFilter)
                }
                
                
                val eventTypeMatch = when (eventTypeFilter) {
                    "call" -> item.isCall
                    "sms" -> item.isSms
                    else -> true 
                }
                
                
                val eventDirectionMatch = when (eventDirectionFilter) {
                    "incoming" -> item.isIncoming
                    "outgoing" -> item.isOutgoing
                    else -> true 
                }
                
                lineMatch && numberMatch && eventTypeMatch && eventDirectionMatch
            }
        }
        
        _filteredHistory.value = filtered
    }

    

    
    fun getContactName(number: String): String {
        val prefix = "*087"
        var numberToLookup = number
        var detectedPrefix = ""

        
        if (number.startsWith(prefix)) {
            detectedPrefix = prefix
            numberToLookup = number.substring(prefix.length)
        }

        
        val normalizedNumber = normalizePhoneNumber(numberToLookup)
        val contactName = _contactsMap.value[normalizedNumber]

        
        return if (contactName != null) {
            
            
            "$detectedPrefix $contactName".trim()
        } else {
            
            
            number
        }
    }

    
}