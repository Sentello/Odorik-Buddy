package com.odorik.odorikbuddy.ui.history

import android.content.ContentResolver
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.repository.HistoryRepository
import com.odorik.odorikbuddy.domain.usecase.ContactNameResolver
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.model.HistoryItem
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository,
    private val appPreferences: AppPreferences,
    private val getLinesUseCase: GetLinesUseCase,
    private val contactNameResolver: ContactNameResolver,
    private val localeManager: LocaleManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    data class HistoryDisplayItem(
        val item: HistoryItem,
        val isChild: Boolean = false,
        val sourceContactName: String = "",
        val destinationContactName: String = ""
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

    private val _eventTypeFilter = MutableStateFlow<String>("all") // "all", "call", "sms"
    val eventTypeFilter: StateFlow<String> = _eventTypeFilter

    private val _eventDirectionFilter = MutableStateFlow<String>("all") // "all", "incoming", "outgoing"
    val eventDirectionFilter: StateFlow<String> = _eventDirectionFilter

    // 5 minutes in milliseconds
    private val STALE_THRESHOLD = 5 * 60 * 1000L
    private var lastUpdateTimestamp: Long = 0

    // --- NEW: STATE FOR CONTACTS MAP ---
    val contactsMap: StateFlow<Map<String, String>> = contactNameResolver.contactsMap

    private val contactNameCache = mutableMapOf<String, String>()

    init {
        viewModelScope.launch {
            fetchLinesInternal() // Fetch lines first as they might affect filtering
            fetchHistory() // Then fetch history with line filters applied
        }
        refreshIfStale() // Check for stale data on init
    }

    fun fetchHistory(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _isRefreshing.value = true
            if (isRefresh) {
                _error.value = null // Clear previous errors only on refresh attempts
            }


            // For initial load, try to show cached data first
            if (!isRefresh) {
                try {
                    val cachedHistory = repository.getCachedHistory()
                    if (cachedHistory.isNotEmpty()) {
                        // Convert cached data to display items
                        val grouped = cachedHistory.groupBy { it.redirectionParentId }
                        val parents = grouped[null] ?: emptyList()
                        val displayItems = mutableListOf<HistoryDisplayItem>()
                        parents.sortedByDescending { it.date }.forEach { parent ->
                            displayItems.add(
                                HistoryDisplayItem(
                                    item = parent,
                                    isChild = false,
                                    sourceContactName = getContactName(parent.sourceNumber),
                                    destinationContactName = getContactName(parent.destinationNumber)
                                )
                            )
                            val children = grouped[parent.id] ?: emptyList()
                            children.sortedByDescending { it.date }.forEach { child ->
                                displayItems.add(
                                    HistoryDisplayItem(
                                        item = child,
                                        isChild = true,
                                        sourceContactName = getContactName(child.sourceNumber),
                                        destinationContactName = getContactName(child.destinationNumber)
                                    )
                                )
                            }
                        }
                        val standalones = cachedHistory.filter { it.redirectionParentId == null && !parents.contains(it) }
                        standalones.sortedByDescending { it.date }.forEach { standalone ->
                            displayItems.add(HistoryDisplayItem(standalone, false))
                        }
                        _history.value = displayItems
                        applyFilters()
                        _error.value = null // Clear any previous errors when cached data is available
                        _isRefreshing.value = false // Stop the spinner when cached data is displayed
                        return@launch // Don't attempt API call if we have cached data
                    }
                } catch (e: Exception) {
                    // Ignore cache errors, proceed to fetch from API
                }
            }

            try {
                // Fetch history for the user-selected period (default 90 days)
                // Calculate dates right before the API call to avoid time drift
                val days = appPreferences.historyPeriodDays
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                isoFormat.timeZone = TimeZone.getTimeZone("UTC") // Ensure UTC timezone
                val now = Calendar.getInstance(TimeZone.getTimeZone("UTC")) // Use UTC for calculations
                val to = isoFormat.format(now.time)
                now.add(Calendar.DAY_OF_YEAR, -days)
                val from = isoFormat.format(now.time)

                val result = repository.getCombinedHistory(from, to)

                // Save to database for offline caching
                repository.insertHistory(result)

                val grouped = result.groupBy { it.redirectionParentId }
                val parents = grouped[null] ?: emptyList()
                val displayItems = mutableListOf<HistoryDisplayItem>()
                parents.sortedByDescending { it.date }.forEach { parent ->
                    displayItems.add(
                        HistoryDisplayItem(
                            item = parent,
                            isChild = false,
                            sourceContactName = getContactName(parent.sourceNumber),
                            destinationContactName = getContactName(parent.destinationNumber)
                        )
                    )
                    val children = grouped[parent.id] ?: emptyList()
                    children.sortedByDescending { it.date }.forEach { child ->
                        displayItems.add(
                            HistoryDisplayItem(
                                item = child,
                                isChild = true,
                                sourceContactName = getContactName(child.sourceNumber),
                                destinationContactName = getContactName(child.destinationNumber)
                            )
                        )
                    }
                }
                val standalones = result.filter { it.redirectionParentId == null && !parents.contains(it) }
                standalones.sortedByDescending { it.date }.forEach { standalone ->
                    displayItems.add(
                        HistoryDisplayItem(
                            item = standalone,
                            isChild = false,
                            sourceContactName = getContactName(standalone.sourceNumber),
                            destinationContactName = getContactName(standalone.destinationNumber)
                        )
                    )
                }
                _history.value = displayItems
                applyFilters()
                _error.value = null // Clear any previous errors on successful fetch
            } catch (e: Exception) {
                if (isRefresh) {
                    // Only show error on explicit refresh attempts
                    val localizedContext = localeManager.createLocaleContext(context)
                    _error.value = ErrorMessageUtil.standardizeError(e.message, localizedContext)
                }
                // For initial load, keep showing cached data (if any) without error
                android.util.Log.e("HistoryViewModel", "Error fetching history", e)
            } finally {
                _isRefreshing.value = false
                if (_error.value == null) {
                     lastUpdateTimestamp = System.currentTimeMillis()
                }
            }
        }
    }

    fun refreshIfStale() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTimestamp > STALE_THRESHOLD) {
            fetchHistory(isRefresh = true)
        }
    }

    fun fetchLines() {
        viewModelScope.launch {
            fetchLinesInternal()
        }
    }

    private suspend fun fetchLinesInternal() {
        val result = getLinesUseCase.execute()
        result.onSuccess {
            _lines.value = it
            // Do NOT automatically select a line as default - let user choose explicitly
            applyFilters()
        }.onFailure {
            // Only set error if we don't have any history data to show
            if (_history.value.isEmpty()) {
                _error.value = it.message ?: context.getString(R.string.error_fetching_lines)
            }
            // If we have history data, don't show line fetch errors
        }
    }
    
    /**
     * Force refresh of both lines and history to ensure consistency.
     * This is useful when the app starts or when filters need to be reapplied.
     */
    fun refreshData() {
        viewModelScope.launch {
            // First refresh lines, then history
            fetchLinesInternal()
            fetchHistory(isRefresh = true)
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

    fun clearError() {
        _error.value = null
    }

    private fun applyFilters() {
        val lineFilter = _selectedLine.value
        val numberFilter = _filterNumber.value
        val eventTypeFilter = _eventTypeFilter.value
        val eventDirectionFilter = _eventDirectionFilter.value

        val filtered = if (lineFilter == null && numberFilter.isBlank() && eventTypeFilter == "all" && eventDirectionFilter == "all") {
            // No filters applied
            _history.value
        } else {
            _history.value.filter { displayItem ->
                val item = displayItem.item
                
                // Line filter
                val lineMatch = lineFilter?.let {
                    item.line == it.id
                } ?: true
                
                // Number filter (check both source and destination)
                val numberMatch = if (numberFilter.isBlank()) {
                    true
                } else {
                    item.sourceNumber.contains(numberFilter) || item.destinationNumber.contains(numberFilter)
                }
                
                // Event type filter (call/sms)
                val eventTypeMatch = when (eventTypeFilter) {
                    "call" -> item.isCall
                    "sms" -> item.isSms
                    else -> true // "all"
                }
                
                // Event direction filter (incoming/outgoing)
                val eventDirectionMatch = when (eventDirectionFilter) {
                    "incoming" -> item.isIncoming
                    "outgoing" -> item.isOutgoing
                    else -> true // "all"
                }
                
                lineMatch && numberMatch && eventTypeMatch && eventDirectionMatch
            }
        }
        
        _filteredHistory.value = filtered
    }

    // --- START: NEW CONTACTS FUNCTIONS ---


    /**
     * Fetches all contacts with phone numbers from the device and populates the contactsMap.
     * This should be called after READ_CONTACTS permission is granted.
     */
    fun loadContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            contactNameResolver.loadContacts(contentResolver)
        }
    }
    
    /**
     * Public function for the UI to get a contact name for a given number.
     * Uses PhoneNumberUtils for robust phone number comparison and matching.
     * Handles special prefixes like "*087" and supports international/local number formats.
     *
     * Returns the contact name if found, otherwise returns the original number.
     */
    fun getContactName(number: String): String {
        return contactNameResolver.getContactName(number)
    }

    // --- END: NEW CONTACTS FUNCTIONS ---
}