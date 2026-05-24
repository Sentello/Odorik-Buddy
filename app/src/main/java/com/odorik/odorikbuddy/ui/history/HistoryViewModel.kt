package com.odorik.odorikbuddy.ui.history

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.repository.HistoryRepository
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.model.HistoryItem
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val getLinesUseCase: GetLinesUseCase,
    private val localeManager: LocaleManager,
    @ApplicationContext private val context: Context
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

    private val contactNameCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    init {
        viewModelScope.launch {

            fetchLines()
            kotlinx.coroutines.delay(100)
            fetchHistory()
        }
    }

    fun fetchHistory(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _isRefreshing.value = true
            if (isRefresh) {
                _error.value = null
            }
            val user = securePreferences.getUser()
            val password = securePreferences.getPassword()

            if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
                _error.value = "User not logged in or credentials missing"
                _isRefreshing.value = false
                return@launch
            }


            if (!isRefresh) {
                try {
                    val cachedHistory = repository.getCachedHistory()
                    if (cachedHistory.isNotEmpty()) {

                        val grouped = cachedHistory.groupBy { it.redirection_parent_id }
                        val parents = grouped[null] ?: emptyList()
                        val displayItems = mutableListOf<HistoryDisplayItem>()
                        parents.sortedByDescending { it.date }.forEach { parent ->
                            displayItems.add(HistoryDisplayItem(parent, false))
                            val children = grouped[parent.id] ?: emptyList()
                            children.sortedByDescending { it.date }.forEach { child ->
                                displayItems.add(HistoryDisplayItem(child, true))
                            }
                        }
                        val standalones = cachedHistory.filter { it.redirection_parent_id == null && !parents.contains(it) }
                        standalones.sortedByDescending { it.date }.forEach { standalone ->
                            displayItems.add(HistoryDisplayItem(standalone, false))
                        }
                        _history.value = displayItems
                        applyFilters()
                        _error.value = null
                        _isRefreshing.value = false
                        return@launch
                if (_history.value.isEmpty()) {
                    _error.value = it.message ?: "Failed to fetch lines"
                }

                            if (!contacts.containsKey(normalizedNumber)) {
                                contacts[normalizedNumber] = name
                            }
                        }
                    }
                }
            }
            _contactsMap.value = contacts
            contactNameCache.clear()
        }
    }


    fun getContactName(number: String): String {
        return contactNameCache.getOrPut(number) {

            val parsedInput = PhoneNumberUtils.parsePhoneNumber(number)


            val exactName = _contactsMap.value[parsedInput.normalizedNumber]
            if (exactName != null) {
                return@getOrPut if (parsedInput.specialPrefix.isNotEmpty()) {
                    "${parsedInput.specialPrefix} $exactName".trim()
                } else {
                    exactName
                }
            }


            val n1 = parsedInput.normalizedNumber.replace("+", "")
            var foundMatch = number
            for ((contactNumber, contactName) in _contactsMap.value) {
                val n2 = contactNumber.replace("+", "")
                if (n1 == n2 || (n1.length > 8 && n2.length > 8 && (n1.endsWith(n2) || n2.endsWith(n1)))) {
                    foundMatch = if (parsedInput.specialPrefix.isNotEmpty()) {
                        "${parsedInput.specialPrefix} $contactName".trim()
                    } else {
                        contactName
                    }
                    break
                }
            }

            foundMatch
        }
    }


}