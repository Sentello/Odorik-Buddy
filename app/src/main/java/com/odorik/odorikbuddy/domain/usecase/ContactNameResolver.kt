package com.odorik.odorikbuddy.domain.usecase

import android.content.ContentResolver
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactNameResolver @Inject constructor(
    private val loadContactsUseCase: LoadContactsUseCase
) {
    private val _contactsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val contactsMap: StateFlow<Map<String, String>> = _contactsMap.asStateFlow()

    suspend fun loadContacts(contentResolver: ContentResolver) {
        // Always refresh in case contacts changed, or we can check if empty. 
        // Let's just reload whenever called to ensure it's up to date.
        _contactsMap.value = loadContactsUseCase(contentResolver)
    }

    fun getContactName(number: String): String {
        val parsedInput = PhoneNumberUtils.parsePhoneNumber(number)
        for ((contactNumber, contactName) in _contactsMap.value) {
            if (PhoneNumberUtils.areNumbersEqual(parsedInput.normalizedNumber, contactNumber)) {
                return if (parsedInput.specialPrefix.isNotEmpty()) {
                    "${parsedInput.specialPrefix} $contactName".trim()
                } else {
                    contactName
                }
            }
        }
        return number
    }
}
