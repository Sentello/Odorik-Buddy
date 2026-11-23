package com.odorik.odorikbuddy.domain.usecase

import android.content.ContentResolver
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared contact name resolution with O(1) exact match, cheap suffix fallback,
 * and a per-number cache. Heavy contact loading always runs on [Dispatchers.IO].
 */
@Singleton
class ContactNameResolver @Inject constructor(
    private val loadContactsUseCase: LoadContactsUseCase
) {
    private val _contactsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val contactsMap: StateFlow<Map<String, String>> = _contactsMap.asStateFlow()

    private val contactNameCache = mutableMapOf<String, String>()

    @Volatile
    private var lastLoadTimestampMs: Long = 0L

    /** Skip full address-book re-read if we loaded recently (saves IO when switching tabs). */
    private val reloadThrottleMs = 60_000L

    /** Where the address book is read. Tests swap it for their own dispatcher. */
    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    suspend fun loadContacts(contentResolver: ContentResolver, force: Boolean = false) {
        if (!force &&
            _contactsMap.value.isNotEmpty() &&
            System.currentTimeMillis() - lastLoadTimestampMs < reloadThrottleMs
        ) {
            return
        }
        val contacts = withContext(ioDispatcher) {
            loadContactsUseCase(contentResolver)
        }
        synchronized(contactNameCache) {
            // Clear before publishing: collectors may run inline on the assignment below
            // (Dispatchers.Main.immediate) and must not see entries computed against the old map.
            contactNameCache.clear()
            lastLoadTimestampMs = System.currentTimeMillis()
            _contactsMap.value = contacts
        }
    }

    /**
     * Resolves a phone number to a contact display name.
     * Fast path: exact map key after normalize. Slow path: string suffix match only
     * (no libphonenumber match loop). Results are cached per input string.
     */
    fun getContactName(number: String): String {
        if (number.isBlank()) return number

        synchronized(contactNameCache) {
            contactNameCache[number]?.let { return it }
        }

        val contacts = _contactsMap.value
        val resolved = resolveContactName(number, contacts)
        // "Contacts not loaded yet" is not an answer worth remembering.
        if (contacts.isEmpty()) return resolved

        synchronized(contactNameCache) {
            // Skip if a newer map was published while we were resolving.
            if (_contactsMap.value === contacts) contactNameCache[number] = resolved
        }
        return resolved
    }

    private fun resolveContactName(number: String, contacts: Map<String, String>): String {
        if (contacts.isEmpty()) return number

        val parsedInput = PhoneNumberUtils.parsePhoneNumber(number)

        // 1. FAST PATH: O(1) exact match on normalized storage key
        val exactName = contacts[parsedInput.normalizedNumber]
        if (exactName != null) {
            return if (parsedInput.specialPrefix.isNotEmpty()) {
                "${parsedInput.specialPrefix} $exactName".trim()
            } else {
                exactName
            }
        }

        // 2. SLOW PATH: cheap suffix match (string only — no areNumbersEqual / libphonenumber)
        val n1 = parsedInput.normalizedNumber.replace("+", "")
        if (n1.length <= 8) return number

        for ((contactNumber, contactName) in contacts) {
            val n2 = contactNumber.replace("+", "")
            if (n2.length <= 8) continue
            if (n1 == n2 || n1.endsWith(n2) || n2.endsWith(n1)) {
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
