package com.odorik.odorikbuddy.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

/**
 * Utility class for phone number parsing, formatting, and comparison using libphonenumber.
 * Provides robust handling of international and local phone number formats.
 */
object PhoneNumberUtils {

    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    // Common special prefixes that should be preserved
    private val SPECIAL_PREFIXES = arrayOf("*087")

    /**
     * Data class to hold parsed phone number information
     */
    data class ParsedPhoneNumber(
        val originalNumber: String,
        val normalizedNumber: String,
        val specialPrefix: String = "",
        val isValid: Boolean = false,
        val phoneNumber: Phonenumber.PhoneNumber? = null
    )

    /**
     * Parses a phone number and extracts any special prefixes.
     * Returns a ParsedPhoneNumber with normalized components.
     */
    fun parsePhoneNumber(number: String): ParsedPhoneNumber {
        if (number.isBlank()) {
            return ParsedPhoneNumber(number, number)
        }

        var numberToParse = number.trim()
        var detectedPrefix = ""

        // Step 1: Check for and separate special prefixes
        for (prefix in SPECIAL_PREFIXES) {
            if (numberToParse.startsWith(prefix)) {
                detectedPrefix = prefix
                numberToParse = numberToParse.substring(prefix.length)
                break
            }
        }

        // Step 2: Try to parse the phone number with libphonenumber
        return try {
            // Use default locale for parsing, but we could make this configurable
            val defaultRegion = "CZ"
            val phoneNumber = phoneNumberUtil.parse(numberToParse, defaultRegion)

            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                // Format to E.164 standard format for consistent storage
                val normalized = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
                ParsedPhoneNumber(number, normalized, detectedPrefix, true, phoneNumber)
            } else {
                // Invalid number, but still normalize what we can
                val normalized = normalizeBasic(numberToParse)
                ParsedPhoneNumber(number, normalized, detectedPrefix, false, null)
            }
        } catch (e: NumberParseException) {
            // Fallback to basic normalization
            val normalized = normalizeBasic(numberToParse)
            ParsedPhoneNumber(number, normalized, detectedPrefix, false, null)
        }
    }

    /**
     * Normalizes a phone number for storage as a map key.
     * This is the primary method for standardizing phone numbers in contact maps.
     */
    fun normalizeForStorage(number: String): String {
        val parsed = parsePhoneNumber(number)
        return if (parsed.isValid && parsed.phoneNumber != null) {
            parsed.normalizedNumber
        } else {
            // For invalid numbers, use basic normalization
            parsed.normalizedNumber
        }
    }

    /**
     * Compares two phone numbers to determine if they represent the same phone.
     * Handles various formats and international/local representations.
     */
    fun areNumbersEqual(number1: String, number2: String): Boolean {
        if (number1.isBlank() || number2.isBlank()) {
            return number1 == number2
        }

        val parsed1 = parsePhoneNumber(number1)
        val parsed2 = parsePhoneNumber(number2)

        // If both have special prefixes, they must match exactly
        if (parsed1.specialPrefix.isNotEmpty() || parsed2.specialPrefix.isNotEmpty()) {
            if (parsed1.specialPrefix != parsed2.specialPrefix) {
                return false
            }
        }

        // 1. Try strict/NSN match with libphonenumber
        if (parsed1.isValid && parsed2.isValid && parsed1.phoneNumber != null && parsed2.phoneNumber != null) {
            val matchType = phoneNumberUtil.isNumberMatch(parsed1.phoneNumber, parsed2.phoneNumber)
            if (matchType == PhoneNumberUtil.MatchType.EXACT_MATCH || 
                matchType == PhoneNumberUtil.MatchType.NSN_MATCH) {
                return true
            }
        }

        // 2. Fallback: Compare normalized versions (E164)
        if (parsed1.normalizedNumber == parsed2.normalizedNumber) {
            return true
        }

        // 3. Robust Suffix Match (fixes the "Server adds 00420 to everything" issue)
        // If we have "089123456" in contacts and server gives "00420089123456", this will catch it.
        // We require at least 7 digits overlap to be safe (avoid matching short numbers like "123")
        val n1 = parsed1.normalizedNumber.replace("+", "")
        val n2 = parsed2.normalizedNumber.replace("+", "")
        
        if (n1.length > 8 && n2.length > 8) {
             if (n1.endsWith(n2) || n2.endsWith(n1)) {
                 return true
             }
        }
        
        return false
    }

    /**
     * Formats a phone number for display purposes.
     * Attempts to use international format, falls back to original if parsing fails.
     */
    fun formatForDisplay(number: String): String {
        val parsed = parsePhoneNumber(number)

        return if (parsed.isValid && parsed.phoneNumber != null) {
            try {
                // Format in international format
                val formatted = phoneNumberUtil.format(
                    parsed.phoneNumber,
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
                )
                // Reconstruct with special prefix if present
                if (parsed.specialPrefix.isNotEmpty()) {
                    "${parsed.specialPrefix} $formatted"
                } else {
                    formatted
                }
            } catch (e: Exception) {
                number
            }
        } else {
            // Return original number if we can't format it
            number
        }
    }

    /**
     * Basic normalization fallback when libphonenumber parsing fails.
     * Similar to the original implementation but improved.
     */
    private fun normalizeBasic(number: String): String {
        var normalized = number.trim()

        // Replace leading 00 with +
        if (normalized.startsWith("00")) {
            normalized = "+${normalized.substring(2)}"
        }

        // Remove all non-numeric characters except the leading '+'
        return normalized.replace(Regex("[^0-9+]"), "")
    }

    /**
     * Checks if a phone number is valid according to libphonenumber.
     */
    fun isValidNumber(number: String): Boolean {
        return parsePhoneNumber(number).isValid
    }

    /**
     * Gets the country code for a phone number if available.
     */
    fun getCountryCode(number: String): String? {
        val parsed = parsePhoneNumber(number)
        return if (parsed.isValid && parsed.phoneNumber != null) {
            parsed.phoneNumber.countryCode.toString()
        } else {
            null
        }
    }
}