package com.odorik.odorikbuddy.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OdorikResponseParserTest {

    @Test
    fun `normal body is success`() {
        val result = OdorikResponseParser.parsePlainTextBody("successfully_added")
        assertTrue(result.isSuccess)
        assertEquals("successfully_added", result.getOrNull())
    }

    @Test
    fun `null body is empty success`() {
        val result = OdorikResponseParser.parsePlainTextBody(null)
        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull())
    }

    @Test
    fun `error body is failure with original message`() {
        val result = OdorikResponseParser.parsePlainTextBody("error authentication_failed")
        assertTrue(result.isFailure)
        assertEquals("error authentication_failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `error missing_argument body is failure`() {
        assertTrue(OdorikResponseParser.parsePlainTextBody("error missing_argument caller").isFailure)
    }
}
