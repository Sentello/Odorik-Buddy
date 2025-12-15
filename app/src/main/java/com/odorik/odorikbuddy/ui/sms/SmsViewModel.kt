package com.odorik.odorikbuddy.ui.sms

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.domain.usecase.SendSmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.odorik.odorikbuddy.data.local.SecurePreferences
import java.time.Instant
import java.time.format.DateTimeParseException

@HiltViewModel
class SmsViewModel @Inject constructor(
    private val sendSmsUseCase: SendSmsUseCase,
    private val getLinesUseCase: GetLinesUseCase,
    private val userRepository: UserRepository,
    private val securePreferences: SecurePreferences,
    private val api: OdorikApi,
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

    fun fetchAllowedSenders() = viewModelScope.launch {
        _error.value = null 
        try {
            val user = securePreferences.getUser()
            val password = securePreferences.getPassword()

            if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
                _error.value = context.getString(R.string.auth_credentials_not_set)
                println("SmsViewModel: Authentication credentials are null or empty.") 
                return@launch
            }

            println("SmsViewModel: Fetching allowed senders with user: $user, password: ${password.take(3)}...") 
            val response = api.getAllowedSenders(user, password)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.startsWith("error") == true) {
                    _error.value = body
                } else {
                    _allowedSenders.value = body?.split(",") ?: emptyList()
                }
            } else {
                _error.value = "HTTP error: ${response.code()}"
            }
        } catch (e: Exception) {
            _error.value = "Network error: ${e.message}"
        }
    }

    fun sendSms(recipient: String, message: String, sender: String?) = viewModelScope.launch {
        _sendResult.value = null 
        _error.value = null      
        try {
            val user = securePreferences.getUser()
            val password = securePreferences.getPassword()

            if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
                _error.value = context.getString(R.string.auth_credentials_not_set)
                return@launch
            }

            val response = api.sendSms(user, password, recipient, message, sender, _delayed.value.takeIf { it.isNotBlank() })
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.startsWith("error") == true) {
                    _error.value = body
                } else {
                    _sendResult.value = body  
                }
            } else {
                _error.value = "HTTP error: ${response.code()}"
            }
        } catch (e: Exception) {
            _error.value = "Network error: ${e.message}"
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

        
        try {
            val minutes = delayed.toInt()
            _delayedError.value = if (minutes > 0) null else R.string.sms_error_invalid_delay_format_client
        } catch (e: NumberFormatException) {
            
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
}