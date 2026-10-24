package com.odorik.odorikbuddy.domain.usecase

import android.content.ContentResolver
import android.provider.ContactsContract
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import javax.inject.Inject

internal fun distinctPhoneNumbers(numbers: List<String>): List<String> =
    numbers.distinctBy { number ->
        val parsed = PhoneNumberUtils.parsePhoneNumber(number)
        parsed.specialPrefix + parsed.normalizedNumber
    }

class GetPhoneNumbersForContactUseCase @Inject constructor() {

    data class ContactPhoneNumbers(
        val displayName: String,
        val numbers: List<String>
    )

    operator fun invoke(contactId: String, contentResolver: ContentResolver): ContactPhoneNumbers? {
        val numbers = mutableListOf<String>()
        var displayName = ""

        contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(contactId),
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                if (nameIndex >= 0) displayName = cursor.getString(nameIndex)
            }
        }

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (numberIndex >= 0) {
                while (cursor.moveToNext()) {
                    val number = cursor.getString(numberIndex)
                    if (!number.isNullOrBlank()) numbers.add(number)
                }
            }
        }

        return if (numbers.isEmpty()) null else ContactPhoneNumbers(displayName, distinctPhoneNumbers(numbers))
    }

    operator fun invoke(contentResolver: ContentResolver, contactUri: android.net.Uri): List<String> {
        var contactId: String? = null
        contentResolver.query(contactUri, arrayOf(ContactsContract.Contacts._ID), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            }
        }
        return contactId?.let { invoke(it, contentResolver)?.numbers } ?: emptyList()
    }
}
