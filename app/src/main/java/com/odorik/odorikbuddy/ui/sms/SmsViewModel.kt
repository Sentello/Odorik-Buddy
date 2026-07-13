package com.odorik.odorikbuddy.ui.sms

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.repository.SmsRepository
import com.odorik.odorikbuddy.domain.usecase.ContactNameResolver
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.domain.usecase.GetPhoneNumbersForContactUseCase
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import com.odorik.odorikbuddy.util.SmsDraftHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class SmsViewModel @Inject constructor(

    private val contactNameResolver: ContactNameResolver,
    private val getPhoneNumbersForContactUseCase: GetPhoneNumbersForContactUseCase,
    private val getLinesUseCase: GetLinesUseCase,
    private val smsRepository: SmsRepository,
    private val smsDraftHelper: SmsDraftHelper,
    private val localeManager: LocaleManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _allowedSenders = MutableStateFlow<List<String>>(emptyList())
    val allowedSenders: StateFlow<List<String>> = _allowedSenders

    private val _sendResult = MutableStateFlow<String?>(null)
    val sendResult: StateFlow<String?> = _sendResult

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _delayed = MutableStateFlow("")
    val delayed: StateFlow<String> = _delayed

    private val _delayedError = MutableStateFlow<Int?>(null)
    val delayedError: StateFlow<Int?> = _delayedError

    private val _isRetrying = MutableStateFlow(false)
    val isRetrying: StateFlow<Boolean> = _isRetrying

    // --- State for inputs to support Contact Resolution ---
    private val _recipient = MutableStateFlow("")
    val recipient: StateFlow<String> = _recipient

    val recipientContactName: StateFlow<String?> = combine(_recipient, contactNameResolver.contactsMap) { number, _ ->
        if (number.isBlank()) null else contactNameResolver.getContactName(number).takeIf { it != number }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Auto-manage error retry when error changes
        viewModelScope.launch {
            error.collect { currentError ->
                if (!currentError.isNullOrEmpty()) {
                    startErrorRetry()
                } else {
                    stopErrorRetry()
                }
            }
        }
    }

    fun updateRecipient(newRecipient: String) {
        _recipient.value = newRecipient
    }

    fun startErrorRetry() {
        if (_isRetrying.value) return
        _isRetrying.value = true
        viewModelScope.launch {
            while (_isRetrying.value) {
                kotlinx.coroutines.delay(5000)
                if (!_isRetrying.value) break
                fetchAllowedSenders()
                if (_error.value == null) {
                    _isRetrying.value = false
                }
            }
        }
    }

    fun stopErrorRetry() {
        _isRetrying.value = false
    }

    fun loadContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            contactNameResolver.loadContacts(contentResolver)
        }
    }

    fun fetchAllowedSenders() = viewModelScope.launch {
        _error.value = null // Clear previous error
        val result = smsRepository.getAllowedSenders()
        result.onSuccess {
            _allowedSenders.value = it
            _error.value = null
        }.onFailure { e ->
            val localizedContext = localeManager.createLocaleContext(context)
            _error.value = ErrorMessageUtil.standardizeError(e.message, localizedContext)
        }
    }

    fun sendSms(recipient: String, message: String, sender: String?) = viewModelScope.launch {
        _sendResult.value = null // Clear previous result
        _error.value = null      // Clear previous error
        
        val result = smsRepository.sendSms(
            recipient = recipient,
            message = message,
            sender = sender,
            delayed = _delayed.value.takeIf { it.isNotBlank() }
        )
        
        result.onSuccess {
            _sendResult.value = it
            clearDraft()
        }.onFailure { e ->
            val localizedContext = localeManager.createLocaleContext(context)
            _error.value = ErrorMessageUtil.standardizeError(e.message, localizedContext)
        }
    }

    fun getPhoneNumbersFromContact(contentResolver: ContentResolver, contactUri: Uri): List<String> {
        return getPhoneNumbersForContactUseCase(contentResolver, contactUri)
    }

    fun onMinutesDelayedInputChange(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            _delayed.value = newValue
            validateDelayedInput(newValue)
        }
    }

    fun validateDelayedInput(delayed: String) {
        if (delayed.isBlank()) {
            _delayedError.value = null
            return
        }

        // Try parse as minutes (positive integer)
        try {
            val minutes = delayed.toInt()
            _delayedError.value = if (minutes > 0) null else R.string.sms_error_invalid_delay_format_client
        } catch (e: NumberFormatException) {
            // Try parse as datetime
            try {
                val scheduled = Instant.parse(delayed)
                val now = Instant.now()
                _delayedError.value = if (scheduled.isAfter(now)) null else R.string.sms_error_delayed_past_client
            } catch (e: DateTimeParseException) {
                _delayedError.value = R.string.sms_error_invalid_delay_format_client
            }
        }
    }

    fun setDateTimeDelayed(newValue: String) {
        _delayed.value = newValue
        validateDelayedInput(newValue)
    }

    fun loadDraft(): Triple<String, String, String?> {
        return smsDraftHelper.loadDraft()
    }

    fun saveDraft(recipient: String, message: String, sender: String?) {
        smsDraftHelper.saveDraft(recipient, message, sender)
    }

    fun clearDraft() {
        smsDraftHelper.clearDraft()
    }
}