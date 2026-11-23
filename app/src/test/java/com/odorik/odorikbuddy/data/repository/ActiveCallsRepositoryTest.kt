package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.util.OdorikApiException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import retrofit2.Response

class ActiveCallsRepositoryTest {

    private val userRepository: UserRepository = mock {
        on { requireCredentials() } doReturn ("user" to "pass")
    }

    private fun repo(listBody: String = "[]", hangUpBody: String = ""): ActiveCallsRepository {
        val api: OdorikApi = mock {
            onBlocking { getActiveCalls(any(), any()) } doReturn Response.success(listBody)
            onBlocking { hangUpActiveCall(any(), any(), any()) } doReturn Response.success(hangUpBody)
        }
        return ActiveCallsRepository(api, userRepository)
    }

    // Shape from https://www.odorik.cz/w/api:calls
    private val answered = """
        [{"id":14990661,"source_number":"00420731000111","destination_number":"00420603123456",
          "destination_name":"Mobil CZ","start_date":"2026-11-22T13:02:10Z",
          "answer_date":"2026-11-22T13:02:18Z","price_per_minute":0.0,"line":403366}]
    """.trimIndent()

    @Test
    fun `parses an answered call`() = runBlocking {
        val calls = repo(answered).getActiveCalls().getOrThrow()
        assertEquals(1, calls.size)
        val call = calls.first()
        assertEquals(14990661L, call.id)
        assertEquals("00420603123456", call.destinationNumber)
        assertEquals(403366L, call.line)
        assertTrue(call.isAnswered)
    }

    /** Captured from a real incoming call on 2026-11-22 (numbers anonymised). */
    @Test
    fun `parses a real incoming call response`() = runBlocking {
        val real = """[{"id":213904118,"source_number":"00359800000000","destination_number":"00420800000000","destination_name":"Česká rep. 800","start_date":"2026-11-22T14:22:34Z","answer_date":"2026-11-22T14:22:35Z","price_per_minute":0.0,"line":760503}]"""
        val call = repo(real).getActiveCalls().getOrThrow().single()
        assertEquals(213904118L, call.id)
        assertEquals("00359800000000", call.sourceNumber)
        assertEquals("Česká rep. 800", call.destinationName)
        assertEquals(760503L, call.line)
        assertTrue(call.isAnswered)
    }

    @Test
    fun `null answer date means ringing`() = runBlocking {
        val body = answered.replace("\"2026-11-22T13:02:18Z\"", "null")
        assertFalse(repo(body).getActiveCalls().getOrThrow().first().isAnswered)
    }

    @Test
    fun `missing optional fields do not crash`() = runBlocking {
        val call = repo("""[{"id":5}]""").getActiveCalls().getOrThrow().first()
        assertEquals(5L, call.id)
        assertFalse(call.isAnswered)
    }

    @Test
    fun `empty list and unauthorized`() = runBlocking {
        assertTrue(repo("[]").getActiveCalls().getOrThrow().isEmpty())
        val err = repo("error unauthorized").getActiveCalls().exceptionOrNull()
        assertEquals("unauthorized", (err as OdorikApiException).code)
    }

    @Test
    fun `hang up succeeds on empty body and fails on error`() = runBlocking {
        assertTrue(repo(hangUpBody = "").hangUp(1).isSuccess)
        assertTrue(repo(hangUpBody = "{}").hangUp(1).isSuccess)
        assertTrue(repo(hangUpBody = "error unauthorized").hangUp(1).isFailure)
    }
}
