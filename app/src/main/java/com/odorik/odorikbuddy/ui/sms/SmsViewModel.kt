package com.odorik.odorikbuddy.ui.sms

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.remote.OdorikApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.odorik.odorikbuddy.data.local.SecurePreferences

@HiltViewModel
class SmsViewModel @Inject constructor(
    private val api: OdorikApi,
    private val securePreferences: SecurePreferences
) : ViewModel() {
    private val _allowedSenders = MutableStateFlow<List<String>>(emptyList())
    val allowedSenders: StateFlow<List<String>> = _allowedSenders

    private val _sendResult = MutableStateFlow<String?>(null)
    val sendResult: StateFlow<String?> = _sendResult

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchAllowedSenders() = viewModelScope.launch {
        try {
            val user = securePreferences.getUser()
            val password = securePreferences.getPassword()

            if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
                _error.value = "Authentication credentials not set."
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

    fun sendSms(recipient: String, message: String, sender: String?, delayed: String?) = viewModelScope.launch {
        try {
            val user = securePreferences.getUser()
            val password = securePreferences.getPassword()

            if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
                _error.value = "Authentication credentials not set."
                return@launch
            }

            val response = api.sendSms(user, password, recipient, message, sender, delayed)
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