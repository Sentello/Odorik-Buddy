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
import com.odorik.odorikbuddy.util.ApiDates
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
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

    private val _filterNumber = MutableStateFlow("")
    val filterNumber: StateFlow<String> = _filterNumber

    private val _eventTypeFilter = MutableStateFlow("all")
    val eventTypeFilter: StateFlow<String> = _eventTypeFilter

    private val _eventDirectionFilter = MutableStateFlow("all")
    val eventDirectionFilter: StateFlow<String> = _eventDirectionFilter

    private val STALE_THRESHOLD = 5 * 60 * 1000L
    private var lastUpdateTimestamp: Long = 0

    val contactsMap: StateFlow<Map<String, String>> = contactNameResolver.contactsMap

    init {

        viewModelScope.launch {
            fetchLinesInternal()
            fetchHistoryInternal(isRefresh = false)
        }


        viewModelScope.launch {
            contactNameResolver.contactsMap
                .drop(1)
                .collect {
                    reapplyContactNames()
                }
        }
    }

    fun fetchHistory(isRefresh: Boolean = false) {
        viewModelScope.launch {
            fetchHistoryInternal(isRefresh)
        }
    }

    private suspend fun fetchHistoryInternal(isRefresh: Boolean = false) {
        _isRefreshing.value = true
        if (isRefresh) {
            _error.value = null
        }

        try {
            if (!isRefresh) {
                val cachedHistory = withContext(Dispatchers.IO) {
                    repository.getCachedHistory()
                }
                if (cachedHistory.isNotEmpty()) {
                    val cachedDisplay = withContext(Dispatchers.Default) {
                        buildDisplayItems(cachedHistory)
                    }
                    _history.value = cachedDisplay
                    applyFilters()
                    _error.value = null

                    if (lastUpdateTimestamp != 0L &&
                        System.currentTimeMillis() - lastUpdateTimestamp <= STALE_THRESHOLD
                    ) {
                        _isRefreshing.value = false
                        return
                    }

                }
            }

            val result = withContext(Dispatchers.IO) {
                val days = appPreferences.getHistoryPeriodDaysSuspend()
                val now = Instant.now()
                val to = ApiDates.formatUtc(now)
                val from = ApiDates.formatUtc(now.minus(days.toLong(), ChronoUnit.DAYS))

                val items = repository.getCombinedHistory(from, to)
                repository.insertHistory(items)

                repository.pruneHistoryBefore(from)
                items
            }

            val displayItems = withContext(Dispatchers.Default) {
                buildDisplayItems(result)
            }
            _history.value = displayItems
            applyFilters()
            _error.value = null
            lastUpdateTimestamp = System.currentTimeMillis()
        } catch (e: Exception) {
            if (isRefresh || _history.value.isEmpty()) {
                val localizedContext = localeManager.createLocaleContext(context)
                _error.value = ErrorMessageUtil.standardizeError(e, localizedContext)
            }
            android.util.Log.e("HistoryViewModel", "Error fetching history", e)
        } finally {
            _isRefreshing.value = false
        }
    }

    fun refreshIfStale() {
        val currentTime = System.currentTimeMillis()
        if (lastUpdateTimestamp == 0L || currentTime - lastUpdateTimestamp > STALE_THRESHOLD) {
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
            applyFilters()
        }.onFailure {
            if (_history.value.isEmpty()) {
                _error.value = it.message ?: context.getString(R.string.error_fetching_lines)
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            fetchLinesInternal()
            fetchHistoryInternal(isRefresh = true)
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

        val filtered = if (lineFilter == null && numberFilter.isBlank() &&
            eventTypeFilter == "all" && eventDirectionFilter == "all"
        ) {
            _history.value
        } else {
            _history.value.filter { displayItem ->
                val item = displayItem.item

                val lineMatch = lineFilter?.let { item.line == it.id } ?: true

                val numberMatch = if (numberFilter.isBlank()) {
                    true
                } else {
                    item.sourceNumber.contains(numberFilter) ||
                        item.destinationNumber.contains(numberFilter)
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

    fun loadContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            contactNameResolver.loadContacts(contentResolver)
        }
    }

    fun getContactName(number: String): String {
        return contactNameResolver.getContactName(number)
    }

    private fun buildDisplayItems(result: List<HistoryItem>): List<HistoryDisplayItem> {
        val grouped = result.groupBy { it.redirectionParentId }
        val parents = grouped[null] ?: emptyList()
        val displayItems = mutableListOf<HistoryDisplayItem>()
        val parentIds = parents.map { it.id }.toHashSet()

        parents.sortedByDescending { it.date }.forEach { parent ->
            displayItems.add(
                HistoryDisplayItem(
                    item = parent,
                    isChild = false,
                    sourceContactName = contactNameResolver.getContactName(parent.sourceNumber),
                    destinationContactName = contactNameResolver.getContactName(parent.destinationNumber)
                )
            )
            val children = grouped[parent.id] ?: emptyList()
            children.sortedByDescending { it.date }.forEach { child ->
                displayItems.add(
                    HistoryDisplayItem(
                        item = child,
                        isChild = true,
                        sourceContactName = contactNameResolver.getContactName(child.sourceNumber),
                        destinationContactName = contactNameResolver.getContactName(child.destinationNumber)
                    )
                )
            }
        }


        val orphanChildren = result.filter { item ->
            val parentId = item.redirectionParentId
            parentId != null && parentId !in parentIds
        }
        orphanChildren.sortedByDescending { it.date }.forEach { item ->
            displayItems.add(
                HistoryDisplayItem(
                    item = item,
                    isChild = true,
                    sourceContactName = contactNameResolver.getContactName(item.sourceNumber),
                    destinationContactName = contactNameResolver.getContactName(item.destinationNumber)
                )
            )
        }
        return displayItems
    }

    private suspend fun reapplyContactNames() {
        val current = _history.value
        if (current.isEmpty()) return
        val updated = withContext(Dispatchers.Default) {
            current.map { display ->
                display.copy(
                    sourceContactName = contactNameResolver.getContactName(display.item.sourceNumber),
                    destinationContactName = contactNameResolver.getContactName(display.item.destinationNumber)
                )
            }
        }
        _history.value = updated
        applyFilters()
    }
}
