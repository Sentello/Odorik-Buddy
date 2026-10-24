package com.odorik.odorikbuddy.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class DistinctPhoneNumbersTest {

    @Test
    fun `same czech number in different spellings collapses to the first`() {
        assertEquals(
            listOf("777 123 456"),
            distinctPhoneNumbers(listOf("777 123 456", "+420777123456", "+420 777 123 456"))
        )
    }

    @Test
    fun `different numbers are kept`() {
        assertEquals(
            listOf("777123456", "608111222"),
            distinctPhoneNumbers(listOf("777123456", "608111222"))
        )
    }

    @Test
    fun `special prefix keeps numbers apart`() {
        assertEquals(
            listOf("*087777123456", "777123456"),
            distinctPhoneNumbers(listOf("*087777123456", "777123456"))
        )
    }

    @Test
    fun `double-zero and plus prefixes collapse`() {
        assertEquals(
            listOf("00420777123456"),
            distinctPhoneNumbers(listOf("00420777123456", "+420777123456"))
        )
    }

    @Test
    fun `foreign number with plus and double-zero collapses`() {
        assertEquals(
            listOf("+49 30 1234567"),
            distinctPhoneNumbers(listOf("+49 30 1234567", "0049301234567"))
        )
    }

    @Test
    fun `foreign number in different formatting collapses`() {
        assertEquals(
            listOf("+421 907 123 456"),
            distinctPhoneNumbers(listOf("+421 907 123 456", "+421907123456"))
        )
    }

    @Test
    fun `same digits in different countries are kept`() {
        assertEquals(
            listOf("+421907123456", "+420907123456"),
            distinctPhoneNumbers(listOf("+421907123456", "+420907123456"))
        )
    }

    @Test
    fun `empty list stays empty`() {
        assertEquals(emptyList<String>(), distinctPhoneNumbers(emptyList()))
    }
}
