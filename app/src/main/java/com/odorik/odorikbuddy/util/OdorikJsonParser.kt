package com.odorik.odorikbuddy.util

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import java.lang.reflect.Type

/** An error code reported by Odorik, e.g. "unauthorized", "invalid_number". */
class OdorikApiException(val code: String) : Exception(code)

/**
 * Parses the JSON Odorik endpoints added in 1.6.0.
 *
 * Odorik often answers HTTP 200 with an error: plain text (`error unauthorized`) or JSON
 * (`{"errors":["invalid_number"]}`). A Gson-typed Retrofit method would throw a parse
 * exception and lose the code, so these endpoints return `Response<String>` and come here.
 */
object OdorikJsonParser {
    private val gson = Gson()

    /**
     * Returns the Odorik error code in [body], or null when the body is not an error.
     * Handles plain text ("error unauthorized") and JSON ({"errors":["a","b"]} → "a").
     */
    fun errorCodeOf(body: String?): String? {
        val text = body?.trim().orEmpty()
        if (text.startsWith("error")) {
            return text.removePrefix("error").trim().substringBefore(' ').ifEmpty { "unknown" }
        }
        if (!text.startsWith("{")) return null
        return try {
            val obj = JsonParser.parseString(text).asJsonObject
            val errors = obj.getAsJsonArray("errors") ?: return null
            errors.firstOrNull()?.asString ?: "unknown"
        } catch (e: Exception) {
            null
        }
    }

    /** Parses [body] as [type], or fails with [OdorikApiException] / the parse error. */
    fun <T> parse(body: String?, type: Type): Result<T> {
        errorCodeOf(body)?.let { return Result.failure(OdorikApiException(it)) }
        return try {
            val value = gson.fromJson<T>(body, type)
                ?: return Result.failure(JsonSyntaxException("Empty body"))
            Result.success(value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** For writes whose success body doesn't matter ("ok", "{}", empty). */
    fun parseAck(body: String?): Result<Unit> =
        errorCodeOf(body)?.let { Result.failure(OdorikApiException(it)) } ?: Result.success(Unit)
}
