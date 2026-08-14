package com.odorik.odorikbuddy.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberUtilsTest {

    @Test
    fun `czech national number normalizes to E164`() {
        assertEquals("+420777123456", PhoneNumberUtils.normalizeForStorage("777 123 456"))
    }

    @Test
    fun `double-zero international prefix normalizes like plus`() {
        assertEquals("+420777123456", PhoneNumberUtils.normalizeForStorage("00420777123456"))
    }

    @Test
    fun `same number in different formats is equal`() {
        assertTrue(PhoneNumberUtils.areNumbersEqual("777 123 456", "+420777123456"))
        assertTrue(PhoneNumberUtils.areNumbersEqual("00420777123456", "+420 777 123 456"))
    }

    @Test
    fun `different numbers are not equal`() {
        assertFalse(PhoneNumberUtils.areNumbersEqual("+420777123456", "+420777123457"))
    }

    @Test
    fun `special prefix is detected and preserved`() {
        val parsed = PhoneNumberUtils.parsePhoneNumber("*087777123456")
        assertEquals("*087", parsed.specialPrefix)
        assertEquals("+420777123456", parsed.normalizedNumber)
    }

    @Test
    fun `prefix mismatch means not equal`() {
        assertFalse(PhoneNumberUtils.areNumbersEqual("*087777123456", "777123456"))
    }

    @Test
    fun `blank inputs compare by identity`() {
        assertTrue(PhoneNumberUtils.areNumbersEqual("", ""))
        assertFalse(PhoneNumberUtils.areNumbersEqual("", "777123456"))
    }
}
