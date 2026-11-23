package com.odorik.odorikbuddy.data.repository

import com.google.gson.reflect.TypeToken
import com.odorik.odorikbuddy.data.model.SimCard
import com.odorik.odorikbuddy.data.model.SimMobileData
import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.util.ApiDates
import com.odorik.odorikbuddy.util.OdorikApiException
import com.odorik.odorikbuddy.util.OdorikJsonParser
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** Odorik mobile SIM cards. The path `number` is `phone_number` exactly as Odorik returns it. */
@Singleton
class SimRepository @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {

    suspend fun getSimCards(): Result<List<SimCard>> = call(
        { u, p -> odorikApi.getSimCards(u, p) },
        { OdorikJsonParser.parse(it, SIM_LIST_TYPE) }
    )

    suspend fun getSimCard(number: String): Result<SimCard> = call(
        { u, p -> odorikApi.getSimCard(number, u, p) },
        { OdorikJsonParser.parse(it, SimCard::class.java) }
    )

    suspend fun getMobileData(number: String, from: Instant, to: Instant): Result<List<SimMobileData>> = call(
        { u, p -> odorikApi.getSimMobileData(number, u, p, ApiDates.formatUtc(from), ApiDates.formatUtc(to)) },
        { OdorikJsonParser.parse(it, MOBILE_DATA_TYPE) }
    )

    /** PUT with the given fields only (e.g. `mobile_data=true`, `state=suspended`). */
    suspend fun update(number: String, fields: Map<String, String>): Result<Unit> = call(
        { u, p -> odorikApi.updateSimCard(number, u, p, fields) },
        OdorikJsonParser::parseAck
    )

    /** FUP reset (paid). Success body is "ok". */
    suspend fun restartData(number: String): Result<Unit> = call(
        { u, p -> odorikApi.restartSimData(number, u, p) },
        OdorikJsonParser::parseAck
    )

    private suspend fun <T> call(
        request: suspend (String, String) -> Response<String>,
        parse: (String?) -> Result<T>
    ): Result<T> {
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = request(userId, password)
            if (!response.isSuccessful) {
                val code = OdorikJsonParser.errorCodeOf(response.errorBody()?.string())
                return Result.failure(code?.let { OdorikApiException(it) } ?: Exception("HTTP error: ${response.code()}"))
            }
            parse(response.body())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        val SIM_LIST_TYPE = object : TypeToken<List<SimCard>>() {}.type
        val MOBILE_DATA_TYPE = object : TypeToken<List<SimMobileData>>() {}.type
    }
}

private val PRAGUE: ZoneId = ZoneId.of("Europe/Prague")
private val PACKAGE_SIZE = Regex("""^(\d+(?:[.,]\d+)?)\s*(MB|GB)$""", RegexOption.IGNORE_CASE)

/**
 * Data package limit in bytes from its name: "3GB" → 3 GiB, "150MB" → 150 MiB.
 * "perKB_…" (pay per kB), "none", unknown names and null → null (no progress bar).
 */
fun dataPackageLimitBytes(name: String?): Long? {
    val text = name?.trim() ?: return null
    if (text.startsWith("perKB", ignoreCase = true)) return null
    val match = PACKAGE_SIZE.matchEntire(text) ?: return null
    val value = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
    val unit = if (match.groupValues[2].equals("GB", ignoreCase = true)) 1024L * 1024 * 1024 else 1024L * 1024
    return (value * unit).toLong()
}

private fun SimMobileData.localDate(): LocalDate? =
    date?.let(ApiDates::parse)?.atZone(PRAGUE)?.toLocalDate()

/** Bytes used in the calendar month of [today] (Europe/Prague). */
fun bytesThisMonth(records: List<SimMobileData>, today: LocalDate): Long =
    records.filter { r -> r.localDate()?.let { it.year == today.year && it.month == today.month } == true }
        .sumOf { it.bytesTotal ?: 0L }

/** Bytes per day for the [days] days ending with [today], oldest first (missing days = 0). */
fun bytesPerDay(records: List<SimMobileData>, today: LocalDate, days: Int = 10): List<Long> {
    val byDay = records.groupBy { it.localDate() }.mapValues { (_, v) -> v.sumOf { it.bytesTotal ?: 0L } }
    return (days - 1 downTo 0).map { byDay[today.minusDays(it.toLong())] ?: 0L }
}
