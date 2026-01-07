package com.odorik.odorikbuddy.ui.calls

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.data.model.CallInfo
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.domain.usecase.CallUseCase
import com.odorik.odorikbuddy.domain.usecase.CreateRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.GetCallListUseCase
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.domain.usecase.GetSharedPublicNumbersUseCase
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val getCallListUseCase: GetCallListUseCase,
    private val getLinesUseCase: GetLinesUseCase,
    private val callUseCase: CallUseCase,
    private val createRouteUseCase: CreateRouteUseCase,
    private val getSharedPublicNumbersUseCase: GetSharedPublicNumbersUseCase,
    private val securePreferences: SecurePreferences,
    private val localeManager: LocaleManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _callList = MutableStateFlow<List<CallInfo>>(emptyList())
    val callList: StateFlow<List<CallInfo>> = _callList

    private val _callResult = MutableStateFlow<String>("")
    val callResult: StateFlow<String> = _callResult

    private val _lines = MutableStateFlow<List<Line>>(emptyList())
    val lines: StateFlow<List<Line>> = _lines

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _callerId = MutableStateFlow("")
    val callerId: StateFlow<String> = _callerId

    private val _recipient = MutableStateFlow("")
    val recipient: StateFlow<String> = _recipient

    private val _selectedLine = MutableStateFlow<Int?>(null)
    val selectedLine: StateFlow<Int?> = _selectedLine

    private val _oneShotCallResult = MutableStateFlow<String>("")
    val oneShotCallResult: StateFlow<String> = _oneShotCallResult

    private val _oneShotCallError = MutableStateFlow<String?>("")
    val oneShotCallError: StateFlow<String?> = _oneShotCallError

    private val _isOneShotCallLoading = MutableStateFlow(false)
    val isOneShotCallLoading: StateFlow<Boolean> = _isOneShotCallLoading

    private val _useCallerIdPrefix = MutableStateFlow(getUseCallerIdPrefix())
    val useCallerIdPrefix: StateFlow<Boolean> = _useCallerIdPrefix

    private fun getUseCallerIdPrefix(): Boolean {
        return securePreferences.getString("use_caller_id_prefix", "false")?.toBoolean() ?: false
    }

    private val _selectedTab = MutableStateFlow(getSelectedTab())
    val selectedTab: StateFlow<String> = _selectedTab

    private fun getSelectedTab(): String {
        val savedString = securePreferences.getString("calls_selected_tab", null)
        val defaultTitle = "callback_title" 
        val tabOrder = getTabOrder()
        return if (savedString?.toIntOrNull() != null) {
            
            val oldIndex = savedString.toInt()
            val migratedTitle = if (oldIndex in tabOrder.indices) tabOrder[oldIndex] else defaultTitle
            
            securePreferences.saveString("calls_selected_tab", migratedTitle)
            migratedTitle
        } else {
            
            savedString?.takeIf { it in tabOrder } ?: defaultTitle
        }
    }

    private val _tabOrder = MutableStateFlow(getTabOrder())
    val tabOrder: StateFlow<List<String>> = _tabOrder

    private fun getTabOrder(): List<String> {
        val savedOrder = securePreferences.getString("calls_tab_order", null)
        return savedOrder?.split(",")?.filter { it.isNotBlank() } ?: listOf(
            "callback_title", "oneshot_call"
        )
    }

    fun updateTabOrder(newOrder: List<String>) {
        _tabOrder.value = newOrder
        securePreferences.saveString("calls_tab_order", newOrder.joinToString(","))
    }

    fun getTabIndexByTitle(title: String): Int {
        return _tabOrder.value.indexOf(title).takeIf { it >= 0 } ?: 0
    }

    private val _phoneNumber = MutableStateFlow(getPhoneNumber())
    val phoneNumber: StateFlow<String> = _phoneNumber

    private fun getPhoneNumber(): String {
        return securePreferences.getString("phone_number", "") ?: ""
    }

    init {
        _callerId.value = securePreferences.getString("caller_id", "") ?: ""
        _recipient.value = securePreferences.getString("recipient", "") ?: ""
        _selectedLine.value = securePreferences.getString("selected_line", null)?.toIntOrNull()
        _useCallerIdPrefix.value = getUseCallerIdPrefix()
        _tabOrder.value = getTabOrder()
        
    }

    fun updateSelectedTab(tabTitle: String) {
        if (tabTitle in _tabOrder.value) {
            _selectedTab.value = tabTitle
            securePreferences.saveString("calls_selected_tab", tabTitle)
        }
    }

    fun updateCallerId(newCallerId: String) {
        _callerId.value = newCallerId
        securePreferences.saveString("caller_id", newCallerId)
    }

    fun updateRecipient(newRecipient: String) {
        _recipient.value = newRecipient
        securePreferences.saveString("recipient", newRecipient)
    }

    fun updateSelectedLine(newLine: Int?) {
        _selectedLine.value = newLine
        securePreferences.saveString("selected_line", newLine?.toString() ?: "")
    }

    fun updateUseCallerIdPrefix(useCallerIdPrefix: Boolean) {
        _useCallerIdPrefix.value = useCallerIdPrefix
        securePreferences.saveString("use_caller_id_prefix", useCallerIdPrefix.toString())
    }

    fun updateSelectedTabByIndex(tabIndex: Int) {
        val tabTitles = _tabOrder.value
        if (tabIndex in tabTitles.indices) {
            val tabTitle = tabTitles[tabIndex]
            updateSelectedTab(tabTitle)
        }
    }

    fun getCallList() {
        
        
    }

    fun getLines() {
        viewModelScope.launch {
            val result = getLinesUseCase.execute()
            result.onSuccess {
                _lines.value = it
                if (_selectedLine.value == null && it.isNotEmpty()) {
                    _selectedLine.value = it.first().id
                }
            }.onFailure {
                val localizedContext = localeManager.createLocaleContext(context)
                _error.value = ErrorMessageUtil.standardizeError(it.message, localizedContext)
            }
        }
    }

    fun makeCall(callerId: String, recipient: String, line: String) {
        viewModelScope.launch {
            _error.value = null 
            _callResult.value = "" 
            val result = callUseCase.execute(callerId, recipient, line)
            result.onSuccess {
                _callResult.value = it
                getCallList()
            }.onFailure {
                _callResult.value = it.message ?: ""
            }
        }
    }

fun getPhoneNumbersFromContact(contentResolver: ContentResolver, contactUri: Uri): List<String> {
    val numbers = mutableListOf<String>()
    contentResolver.query(contactUri, arrayOf(ContactsContract.Contacts._ID), null, null, null)?.use { contactCursor ->
        if (contactCursor.moveToFirst()) {
            val contactId = contactCursor.getString(contactCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val phoneSelection = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
            val phoneSelectionArgs = arrayOf(contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                phoneProjection,
                phoneSelection,
                phoneSelectionArgs,
                null
            )?.use { phoneCursor ->
                while (phoneCursor.moveToNext()) {
                    var number = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    number = number.replace(Regex("[^0-9+]"), "") 
                    if (number.isNotBlank()) {
                        numbers.add(number)
                    }
                }
            }
        }
    }
    return numbers
}

    fun makeOneShotCall(targetRecipient: String, useLineAsCallerId: Boolean) {
        viewModelScope.launch {
            _oneShotCallError.value = null
            _oneShotCallResult.value = ""  
            _isOneShotCallLoading.value = true
            
            try {
                
                val currentPhoneNumber = securePreferences.getString("phone_number", "") ?: ""
                _phoneNumber.value = currentPhoneNumber
                
                
                val publicNumbersResult = getSharedPublicNumbersUseCase.execute()
                if (publicNumbersResult.isFailure) {
                    val localizedContext = localeManager.createLocaleContext(context)
                    _oneShotCallError.value = ErrorMessageUtil.standardizeError("Error getting public numbers: ${publicNumbersResult.exceptionOrNull()?.message}", localizedContext)
                    _isOneShotCallLoading.value = false
                    return@launch
                }
                
                val publicNumbers = publicNumbersResult.getOrNull()
                if (publicNumbers.isNullOrEmpty()) {
                    _oneShotCallError.value = context.getString(com.odorik.odorikbuddy.R.string.oneshot_error_no_shared_numbers_available)
                    _isOneShotCallLoading.value = false
                    return@launch
                }

                
                val lastSharedNumber = publicNumbers.lastOrNull { it.type == "shared" }?.publicNumber
                if (lastSharedNumber == null) {
                    _oneShotCallError.value = context.getString(com.odorik.odorikbuddy.R.string.oneshot_error_no_shared_numbers_found)
                    _isOneShotCallLoading.value = false
                    return@launch
                }
                
                
                
                val sourceNumber = currentPhoneNumber
                
                
                val selectedLineInfo = _selectedLine.value?.let { selectedLineId ->
                    _lines.value.find { it.id == selectedLineId }
                }
                
                if (sourceNumber.isNullOrEmpty()) {
                    _oneShotCallError.value = context.getString(com.odorik.odorikbuddy.R.string.oneshot_error_no_source_number_configured)
                    _isOneShotCallLoading.value = false
                    return@launch
                }
                
                
                val routeResult = if (selectedLineInfo != null) {
                    
                    createRouteUseCase.executeWithLineCredentials(
                        publicNumber = lastSharedNumber,
                        sourceNumber = sourceNumber,
                        ringingNumber = targetRecipient,
                        replaceBySource = true, 
                        useCallerIdPrefix = useLineAsCallerId,
                        lineId = selectedLineInfo.id.toString(),
                        sipPassword = selectedLineInfo.sip_password
                    )
                } else {
                    
                    createRouteUseCase.execute(
                        publicNumber = lastSharedNumber,
                        sourceNumber = sourceNumber,
                        ringingNumber = targetRecipient,
                        replaceBySource = true, 
                        useCallerIdPrefix = useLineAsCallerId
                    )
                }
                
                if (routeResult.isFailure) {
                    val localizedContext = localeManager.createLocaleContext(context)
                    _oneShotCallError.value = ErrorMessageUtil.standardizeError("Error creating route: ${routeResult.exceptionOrNull()?.message}", localizedContext)
                    _isOneShotCallLoading.value = false
                    return@launch
                }
                
                
                _oneShotCallResult.value = lastSharedNumber
            } catch (e: Exception) {
                val localizedContext = localeManager.createLocaleContext(context)
                _oneShotCallError.value = ErrorMessageUtil.standardizeError("Error during One Shot Call setup: ${e.message}", localizedContext)
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
}
