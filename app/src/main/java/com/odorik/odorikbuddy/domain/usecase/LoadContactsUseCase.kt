package com.odorik.odorikbuddy.domain.usecase

import android.content.ContentResolver
import android.provider.ContactsContract
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import javax.inject.Inject

class LoadContactsUseCase @Inject constructor() {

    /**
     * Reads all device contacts and returns a map of normalized number -> display name.
     * Must be called from a coroutine (suspending I/O via ContentResolver).
     */
    operator fun invoke(contentResolver: ContentResolver): Map<String, String> {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        val contacts = mutableMapOf<String, String>()

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null, null
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
        return contacts
    }
}
