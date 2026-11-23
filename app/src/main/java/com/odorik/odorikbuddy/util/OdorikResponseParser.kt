package com.odorik.odorikbuddy.util

/**
 * Parses plain-text Odorik API responses.
 *
 * Many Odorik endpoints return HTTP 200 with a body that starts with `error`
 * (e.g. `error authentication_failed`, `error missing_argument ...`). Callers that
 * only check [retrofit2.Response.isSuccessful] would treat those as success.
 */
object OdorikResponseParser {

    /**
     * @return [Result.success] with the body (empty string if null), or
     * [Result.failure] when the body is an Odorik error payload.
     */
    fun parsePlainTextBody(body: String?): Result<String> {
        val text = body ?: ""
        if (text.startsWith("error")) {
            return Result.failure(Exception(text))
        }
        return Result.success(text)
    }
}
