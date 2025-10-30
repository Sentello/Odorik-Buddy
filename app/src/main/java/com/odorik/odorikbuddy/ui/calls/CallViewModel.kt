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

    init {
        _callerId.value = securePreferences.getString("caller_id", "") ?: ""
        _recipient.value = securePreferences.getString("recipient", "") ?: ""
    }

    fun updateCallerId(newCallerId: String) {
        _callerId.value = newCallerId
        securePreferences.saveString("caller_id", newCallerId)
    }

    fun updateRecipient(newRecipient: String) {
        _recipient.value = newRecipient
        securePreferences.saveString("recipient", newRecipient)
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
            }.onFailure {
                Log.e("CallViewModel", "Error fetching lines: ${it.message}")
                _error.value = it.message
            }
        }
    }

    fun makeCall(callerId: String, recipient: String, line: String) {
        viewModelScope.launch {
            val result = callUseCase.execute(callerId, recipient, line)
            result.onSuccess {
                _callResult.value = "Call initiated successfully"
                getCallList()
            }.onFailure {
                _callResult.value = "Error: ${it.message}"
            }
        }
    }

    fun handleContactSelection(contentResolver: ContentResolver, contactUri: Uri, onPhoneNumberSelected: (String) -> Unit) {
        
        val contactProjection = arrayOf(ContactsContract.Contacts._ID)
        contentResolver.query(contactUri, contactProjection, null, null, null)?.use { contactCursor ->
            if (contactCursor.moveToFirst()) {
                val contactIdIndex = contactCursor.getColumnIndex(ContactsContract.Contacts._ID)
                if (contactIdIndex != -1) {
                    val contactId = contactCursor.getString(contactIdIndex)

                    
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
                        if (phoneCursor.moveToFirst()) {
                            val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numberIndex != -1) {
                                var number = phoneCursor.getString(numberIndex)
                                
                                number = number.replace(Regex("[^0-9+]"), "")
                                onPhoneNumberSelected(number)
                            }
                        }
                        
                    }
                }
            }
        }
    }
}