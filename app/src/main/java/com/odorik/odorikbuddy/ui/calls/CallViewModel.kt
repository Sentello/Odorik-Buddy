package com.odorik.odorikbuddy.ui.calls

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.data.model.CallInfo
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.domain.usecase.CallUseCase
import com.odorik.odorikbuddy.domain.usecase.GetCallListUseCase
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val getCallListUseCase: GetCallListUseCase,
    private val getLinesUseCase: GetLinesUseCase,
    private val callUseCase: CallUseCase,
    private val securePreferences: SecurePreferences
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

    init {
        _callerId.value = securePreferences.getString("caller_id", "") ?: ""
        _recipient.value = securePreferences.getString("recipient", "") ?: ""
        _selectedLine.value = securePreferences.getString("selected_line", null)?.toIntOrNull()
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

    fun getCallList() {
        
        
    }

    fun getLines() {
        Log.d("CallViewModel", "Fetching lines...")
        viewModelScope.launch {
            val result = getLinesUseCase.execute()
            result.onSuccess {
                Log.d("CallViewModel", "Lines fetched successfully: $it")
                _lines.value = it
                if (_selectedLine.value == null && it.isNotEmpty()) {
                    _selectedLine.value = it.first().id
                }
            }.onFailure {
                Log.e("CallViewModel", "Error fetching lines: ${it.message}")
                _error.value = it.message
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
}
