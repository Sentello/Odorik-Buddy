package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.model.SimMobileData
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.util.OdorikApiException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import retrofit2.Response
import java.time.LocalDate

class SimRepositoryTest {

    private val userRepository: UserRepository = mock {
        on { requireCredentials() } doReturn ("user" to "pass")
    }

    private fun repo(list: String = "[]", one: String = "{}", write: String = "{}", restart: String = "ok"): SimRepository {
        val api: OdorikApi = mock {
            onBlocking { getSimCards(any(), any()) } doReturn Response.success(list)
            onBlocking { getSimCard(any(), any(), any()) } doReturn Response.success(one)
            onBlocking { updateSimCard(any(), any(), any(), any()) } doReturn Response.success(write)
            onBlocking { restartSimData(any(), any(), any()) } doReturn Response.success(restart)
        }
        return SimRepository(api, userRepository)
    }

    /** Shape from https://www.odorik.cz/w/api:sim_cards (values made up). */
    private val sim = """{"id":1013,"phone_number":"00420731000111","sim_number":"8942001234567890123",
        "state":"active","changes_in_progress":[],"data_package":"3GB","data_package_for_next_month":"3GB",
        "data_bought_total":null,"data_used":null,"voice_package":"none","voice_package_for_next_month":"none",
        "package_delayed_billing":"none","missed_calls_register":false,"mobile_data":true,"lte":true,
        "lte_for_next_month":true,"roaming":"eu_data_outgoing","premium_services":"off"}"""

    @Test
    fun `parses a documented sim`() = runBlocking {
        val card = repo(one = sim).getSimCard("00420731000111").getOrThrow()
        assertEquals("00420731000111", card.phoneNumber)
        assertTrue(card.isActive)
        assertFalse(card.hasChangesInProgress)
        assertEquals(true, card.mobileData)
        assertEquals("eu_data_outgoing", card.roaming)
    }

    @Test
    fun `empty list means no sim, unauthorized is a failure`() = runBlocking {
        assertTrue(repo(list = "[]").getSimCards().getOrThrow().isEmpty())
        val e = repo(list = "error unauthorized").getSimCards().exceptionOrNull()
        assertEquals("unauthorized", (e as OdorikApiException).code)
    }

    @Test
    fun `unreadable response fails instead of crashing`() = runBlocking {
        assertTrue(repo(list = "<html>oops</html>").getSimCards().isFailure)
    }

    @Test
    fun `writes accept ok and empty object, errors fail`() = runBlocking {
        assertTrue(repo(write = "{}").update("1", mapOf("lte" to "true")).isSuccess)
        assertTrue(repo(restart = "ok").restartData("1").isSuccess)
        val e = repo(restart = "error low_balance").restartData("1").exceptionOrNull()
        assertEquals("low_balance", (e as OdorikApiException).code)
    }

    @Test
    fun `package limits from names`() {
        assertEquals(3L * 1024 * 1024 * 1024, dataPackageLimitBytes("3GB"))
        assertEquals(10L * 1024 * 1024 * 1024, dataPackageLimitBytes("10GB"))
        assertEquals(150L * 1024 * 1024, dataPackageLimitBytes("150MB"))
        assertNull(dataPackageLimitBytes("perKB_50MB"))
        assertNull(dataPackageLimitBytes("none"))
        assertNull(dataPackageLimitBytes(null))
    }

    private fun rec(date: String, bytes: Long) = SimMobileData(1, date, bytes, 0.0, "x")

    @Test
    fun `bytes this month ignore other months`() {
        val records = listOf(
            rec("2026-10-31T10:00:00Z", 999),
            rec("2026-11-01T10:00:00Z", 100),
            rec("2026-11-22T10:00:00Z", 50)
        )
        assertEquals(150L, bytesThisMonth(records, LocalDate.of(2026, 11, 22)))
    }

    @Test
    fun `bytes per day fill missing days with zero, oldest first`() {
        val records = listOf(rec("2026-11-22T10:00:00Z", 7), rec("2026-11-20T10:00:00Z", 3), rec("2026-11-20T12:00:00Z", 2))
        assertEquals(listOf(5L, 0L, 7L), bytesPerDay(records, LocalDate.of(2026, 11, 22), days = 3))
    }
}
