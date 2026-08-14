package com.odorik.odorikbuddy.ui.calls

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.domain.usecase.CallUseCase
import com.odorik.odorikbuddy.domain.usecase.ContactNameResolver
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.domain.usecase.GetPhoneNumbersForContactUseCase
import com.odorik.odorikbuddy.domain.usecase.GetSharedPublicNumbersUseCase
import com.odorik.odorikbuddy.domain.usecase.NoSharedNumbersException
import com.odorik.odorikbuddy.domain.usecase.NoSourceNumberException
import com.odorik.odorikbuddy.domain.usecase.OneShotCallCoordinatorUseCase
import com.odorik.odorikbuddy.domain.usecase.SharedNumberNotFoundException
import com.odorik.odorikbuddy.util.BackoffPolicy
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val contactNameResolver: ContactNameResolver,
    private val getPhoneNumbersForContactUseCase: GetPhoneNumbersForContactUseCase,
    private val getLinesUseCase: GetLinesUseCase,
    private val callUseCase: CallUseCase,
    private val oneShotCallCoordinatorUseCase: OneShotCallCoordinatorUseCase,
    private val getSharedPublicNumbersUseCase: GetSharedPublicNumbersUseCase,
    private val localeManager: LocaleManager,
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _callResult = MutableStateFlow<String>("")
    val callResult: StateFlow<String> = _callResult

    private val _lines = MutableStateFlow<List<Line>>(emptyList())
    val lines: StateFlow<List<Line>> = _lines

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isRetrying = MutableStateFlow(false)
    val isRetrying: StateFlow<Boolean> = _isRetrying

    private val retryBackoff = BackoffPolicy()

    private val _isCallbackLoading = MutableStateFlow(false)
    val isCallbackLoading: StateFlow<Boolean> = _isCallbackLoading

    private val _callerId = MutableStateFlow("")
    val callerId: StateFlow<String> = _callerId

    private val _callbackRecipient = MutableStateFlow("")
    val callbackRecipient: StateFlow<String> = _callbackRecipient

    private val _oneShotRecipient = MutableStateFlow("")
    val oneShotRecipient: StateFlow<String> = _oneShotRecipient

    val directCallsEnabled: Boolean
        get() = appPreferences.directCallsEnabled


    val callerContactName: StateFlow<String?> = combine(_callerId, contactNameResolver.contactsMap) { number, _ ->
        if (number.isBlank()) null else contactNameResolver.getContactName(number).takeIf { it != number }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val callbackRecipientContactName: StateFlow<String?> = combine(_callbackRecipient, contactNameResolver.contactsMap) { number, _ ->
        if (number.isBlank()) null else contactNameResolver.getContactName(number).takeIf { it != number }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val oneShotRecipientContactName: StateFlow<String?> = combine(_oneShotRecipient, contactNameResolver.contactsMap) { number, _ ->
        if (number.isBlank()) null else contactNameResolver.getContactName(number).takeIf { it != number }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedLine = MutableStateFlow<Int?>(null)
    val selectedLine: StateFlow<Int?> = _selectedLine

    private val _oneShotCallResult = MutableStateFlow<String>("")
    val oneShotCallResult: StateFlow<String> = _oneShotCallResult


    private val _dialerLaunchRequest = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val dialerLaunchRequest = _dialerLaunchRequest.asSharedFlow()

    private val _oneShotCallError = MutableStateFlow<String?>("")
    val oneShotCallError: StateFlow<String?> = _oneShotCallError

    private val _isOneShotCallLoading = MutableStateFlow(false)
    val isOneShotCallLoading: StateFlow<Boolean> = _isOneShotCallLoading

    private val _useCallerIdPrefix = MutableStateFlow(getUseCallerIdPrefix())
    val useCallerIdPrefix: StateFlow<Boolean> = _useCallerIdPrefix

    private fun getUseCallerIdPrefix(): Boolean {
        return appPreferences.getString("use_caller_id_prefix", "false")?.toBoolean() ?: false
    }

    private val _selectedTab = MutableStateFlow(getSelectedTab())
    val selectedTab: StateFlow<String> = _selectedTab

    private fun getSelectedTab(): String {
        val savedString = appPreferences.getString("calls_selected_tab", null)
        val defaultTitle = "callback_title"
        val tabOrder = getTabOrder()
        return if (savedString?.toIntOrNull() != null) {

            val oldIndex = savedString.toInt()
            val migratedTitle = if (oldIndex in tabOrder.indices) tabOrder[oldIndex] else defaultTitle

            appPreferences.saveString("calls_selected_tab", migratedTitle)
            migratedTitle
        } else {

            savedString?.takeIf { it in tabOrder } ?: defaultTitle
        }
    }

    private val _tabOrder = MutableStateFlow(getTabOrder())
    val tabOrder: StateFlow<List<String>> = _tabOrder

    private fun getTabOrder(): List<String> {
        val savedOrder = appPreferences.getString("calls_tab_order", null)
        val defaultOrder = listOf("callback_title", "oneshot_call", "tiles_title")

        if (savedOrder == null) return defaultOrder

        val currentList = savedOrder.split(",").filter { it.isNotBlank() }.toMutableList()

        if (!currentList.contains("tiles_title")) {
            currentList.add("tiles_title")
        }

        return currentList
    }

    private val _phoneNumber = MutableStateFlow(getPhoneNumber())
    val phoneNumber: StateFlow<String> = _phoneNumber

    private fun getPhoneNumber(): String {
        return appPreferences.getString("phone_number", "") ?: ""
    }

    fun startErrorRetry() {
        if (_isRetrying.value) return
        _isRetrying.value = true
        viewModelScope.launch {
            var attempt = 0
            while (_isRetrying.value && attempt < retryBackoff.maxAttempts) {
                attempt++
                kotlinx.coroutines.delay(retryBackoff.delayBeforeAttempt(attempt))
                if (!_isRetrying.value) break
                getLinesInternal()
                if (_error.value == null) break
            }
            _isRetrying.value = false
        }
    }

    fun stopErrorRetry() {
        _isRetrying.value = false
    }

    init {

        viewModelScope.launch {
            error.collect { currentError ->
                if (!currentError.isNullOrEmpty()) {
                    startErrorRetry()
                } else {
                    stopErrorRetry()
                }
            }
        }

        _callerId.value = appPreferences.getString("caller_id", "") ?: ""
        _callbackRecipient.value = appPreferences.getString("recipient", "") ?: ""
        _oneShotRecipient.value = appPreferences.getString("oneshot_recipient", "") ?: ""
        _selectedLine.value = appPreferences.getString("selected_line", null)?.toIntOrNull()
        _useCallerIdPrefix.value = getUseCallerIdPrefix()
        _tabOrder.value = getTabOrder()



        getLines()
    }

    fun loadContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            contactNameResolver.loadContacts(contentResolver)
        }
    }

    fun updateTabOrder(newOrder: List<String>) {
        _tabOrder.value = newOrder
        appPreferences.saveString("calls_tab_order", newOrder.joinToString(","))
    }

    fun getTabIndexByTitle(title: String): Int {
        return _tabOrder.value.indexOf(title).takeIf { it >= 0 } ?: 0
    }

    fun updateSelectedTab(tabTitle: String) {
        if (tabTitle in _tabOrder.value) {
            _selectedTab.value = tabTitle
            appPreferences.saveString("calls_selected_tab", tabTitle)
        }
    }

    fun updateCallerId(newCallerId: String) {
        _callerId.value = newCallerId
        appPreferences.saveString("caller_id", newCallerId)
    }

    fun updateCallbackRecipient(newRecipient: String) {
        _callbackRecipient.value = newRecipient
        appPreferences.saveString("recipient", newRecipient)
    }

    fun updateOneShotRecipient(newRecipient: String) {
        _oneShotRecipient.value = newRecipient
        appPreferences.saveString("oneshot_recipient", newRecipient)
    }

    fun updateSelectedLine(newLine: Int?) {
        _selectedLine.value = newLine
        appPreferences.saveString("selected_line", newLine?.toString() ?: "")
    }

    fun updateUseCallerIdPrefix(useCallerIdPrefix: Boolean) {
        _useCallerIdPrefix.value = useCallerIdPrefix
        appPreferences.saveString("use_caller_id_prefix", useCallerIdPrefix.toString())
    }

    fun updateSelectedTabByIndex(tabIndex: Int) {
        val tabTitles = _tabOrder.value
        if (tabIndex in tabTitles.indices) {
            val tabTitle = tabTitles[tabIndex]
            updateSelectedTab(tabTitle)
        }
    }

    fun getLines() {
        viewModelScope.launch { getLinesInternal() }
    }

    private suspend fun getLinesInternal() {
        val result = getLinesUseCase.execute()
        result.onSuccess {
            _lines.value = it
            _error.value = null
            _oneShotCallError.value = null
            _callResult.value = ""
            if (_selectedLine.value == null && it.isNotEmpty()) {
                _selectedLine.value = it.first().id
            }
        }.onFailure {
            val localizedContext = localeManager.createLocaleContext(context)
            _error.value = ErrorMessageUtil.standardizeError(it, localizedContext)
        }
    }

    fun makeCall(callerId: String, recipient: String, line: String) {
        if (_isCallbackLoading.value) return
        _isCallbackLoading.value = true
        viewModelScope.launch {
            try {
                _error.value = null
                _callResult.value = ""
                val result = callUseCase.execute(callerId, recipient, line)
                result.onSuccess {
                    _callResult.value = it
                }.onFailure {
                    val localizedContext = localeManager.createLocaleContext(context)
                    _error.value = ErrorMessageUtil.standardizeError(it, localizedContext)
                }
            } finally {
                _isCallbackLoading.value = false
            }
        }
    }

    fun getPhoneNumbersFromContact(contentResolver: ContentResolver, contactUri: Uri): List<String> {
        return getPhoneNumbersForContactUseCase(contentResolver, contactUri)
    }


    fun makeOneShotCall(
        targetRecipient: String,
        useLineAsCallerId: Boolean,
        selectedLineId: Int? = null
    ) {
        if (_isOneShotCallLoading.value) return
        viewModelScope.launch {
            _oneShotCallError.value = null
            _oneShotCallResult.value = ""
            _isOneShotCallLoading.value = true

            try {

                val lineIdToUse = selectedLineId ?: _selectedLine.value

                val result = oneShotCallCoordinatorUseCase.execute(
                    targetRecipient = targetRecipient,
                    useLineAsCallerId = useLineAsCallerId,
                    selectedLineId = lineIdToUse
                )

                result.onSuccess { lastSharedNumber ->
                    _oneShotCallResult.value = lastSharedNumber
                    _dialerLaunchRequest.tryEmit(lastSharedNumber)
                }.onFailure { e ->
                    val localizedContext = localeManager.createLocaleContext(context)
                    _oneShotCallError.value = when (e) {
                        is NoSourceNumberException -> context.getString(com.odorik.odorikbuddy.R.string.oneshot_error_no_source_number_configured)
                        is NoSharedNumbersException -> context.getString(com.odorik.odorikbuddy.R.string.oneshot_error_no_shared_numbers_available)
                        is SharedNumberNotFoundException -> context.getString(com.odorik.odorikbuddy.R.string.oneshot_error_no_shared_numbers_found)
                        else -> ErrorMessageUtil.standardizeError(e, localizedContext)
                    }
                }
            } catch (e: Exception) {
                val localizedContext = localeManager.createLocaleContext(context)
                _oneShotCallError.value = ErrorMessageUtil.standardizeError(e, localizedContext)
            } finally {
                _isOneShotCallLoading.value = false
            }
        }
    }

    fun resetCallResult() {
        _callResult.value = ""
    }

    fun resetOneShotCallResult() {
        _oneShotCallResult.value = ""
    }

    fun resetOneShotCallError() {
        _oneShotCallError.value = null
    }
}
