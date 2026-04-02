package com.odorik.odorikbuddy.ui.widget

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppWidgetConfigurationViewModel @Inject constructor(
    private val tileRepository: TileRepository
) : ViewModel() {

    val tiles: StateFlow<List<TileEntity>> = tileRepository.getAllTiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _contactsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val contactsMap: StateFlow<Map<String, String>> = _contactsMap.asStateFlow()

    fun loadContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            val contacts = mutableMapOf<String, String>()

            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

                if (numberIndex >= 0 && nameIndex >= 0) {
                    while (cursor.moveToNext()) {
                        val number = cursor.getString(numberIndex)
                        val name = cursor.getString(nameIndex)
                        if (!number.isNullOrBlank() && !name.isNullOrBlank()) {
                            val normalizedNumber = PhoneNumberUtils.normalizeForStorage(number)
                            if (!contacts.containsKey(normalizedNumber)) {
                                contacts[normalizedNumber] = name
                            }
                        }
                    }
                }
            }
            _contactsMap.value = contacts
        }
    }


    fun getContactName(number: String): String {
        val parsedInput = PhoneNumberUtils.parsePhoneNumber(number)

        for ((contactNumber, contactName) in _contactsMap.value) {
            if (PhoneNumberUtils.areNumbersEqual(parsedInput.normalizedNumber, contactNumber)) {
                return contactName
            }
        }

        return number
    }
}
