package com.odorik.odorikbuddy.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber


object PhoneNumberUtils {

    private val phoneNumberUtil = PhoneNumberUtil.getInstance()


    private val SPECIAL_PREFIXES = arrayOf("*087")


    data class ParsedPhoneNumber(
        val originalNumber: String,
        val normalizedNumber: String,
        val specialPrefix: String = "",
        val isValid: Boolean = false,
        val phoneNumber: Phonenumber.PhoneNumber? = null
    )


    fun parsePhoneNumber(number: String): ParsedPhoneNumber {
        if (number.isBlank()) {
            return ParsedPhoneNumber(number, number)
        }

        var numberToParse = number.trim()
        var detectedPrefix = ""


        for (prefix in SPECIAL_PREFIXES) {
            if (numberToParse.startsWith(prefix)) {
                detectedPrefix = prefix
                numberToParse = numberToParse.substring(prefix.length)
                break
            }
        }


        return try {

            val defaultRegion = "CZ"
            val phoneNumber = phoneNumberUtil.parse(numberToParse, defaultRegion)

            if (phoneNumberUtil.isValidNumber(phoneNumber)) {

                val normalized = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
                ParsedPhoneNumber(number, normalized, detectedPrefix, true, phoneNumber)
            } else {

                val normalized = normalizeBasic(numberToParse)
                ParsedPhoneNumber(number, normalized, detectedPrefix, false, null)
            }
        } catch (e: NumberParseException) {

            val normalized = normalizeBasic(numberToParse)
            ParsedPhoneNumber(number, normalized, detectedPrefix, false, null)
        }
    }


    fun normalizeForStorage(number: String): String {
        val parsed = parsePhoneNumber(number)
        return if (parsed.isValid && parsed.phoneNumber != null) {
            parsed.normalizedNumber
        } else {

            parsed.normalizedNumber
        }
    }


    fun areNumbersEqual(number1: String, number2: String): Boolean {
        if (number1.isBlank() || number2.isBlank()) {
            return number1 == number2
        }

        val parsed1 = parsePhoneNumber(number1)
        val parsed2 = parsePhoneNumber(number2)


        if (parsed1.specialPrefix.isNotEmpty() || parsed2.specialPrefix.isNotEmpty()) {
            if (parsed1.specialPrefix != parsed2.specialPrefix) {
                return false
            }
        }


        if (parsed1.isValid && parsed2.isValid && parsed1.phoneNumber != null && parsed2.phoneNumber != null) {
            val matchType = phoneNumberUtil.isNumberMatch(parsed1.phoneNumber, parsed2.phoneNumber)
            if (matchType == PhoneNumberUtil.MatchType.EXACT_MATCH ||
                matchType == PhoneNumberUtil.MatchType.NSN_MATCH) {
                return true
            }
        }


        if (parsed1.normalizedNumber == parsed2.normalizedNumber) {
            return true
        }




        val n1 = parsed1.normalizedNumber.replace("+", "")
        val n2 = parsed2.normalizedNumber.replace("+", "")

        if (n1.length > 8 && n2.length > 8) {
             if (n1.endsWith(n2) || n2.endsWith(n1)) {
                 return true
             }
        }

        return false
    }


    fun formatForDisplay(number: String): String {
        val parsed = parsePhoneNumber(number)

        return if (parsed.isValid && parsed.phoneNumber != null) {
            try {

                val formatted = phoneNumberUtil.format(
                    parsed.phoneNumber,
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
                )

                if (parsed.specialPrefix.isNotEmpty()) {
                    "${parsed.specialPrefix} $formatted"
                } else {
                    formatted
                }
            } catch (e: Exception) {
                number
            }
        } else {

        return normalized.replace(Regex("[^0-9+]"), "")
    }


    fun isValidNumber(number: String): Boolean {
        return parsePhoneNumber(number).isValid
    }


    fun getCountryCode(number: String): String? {
        val parsed = parsePhoneNumber(number)
        return if (parsed.isValid && parsed.phoneNumber != null) {
            parsed.phoneNumber.countryCode.toString()
        } else {
            null
        }
    }
}