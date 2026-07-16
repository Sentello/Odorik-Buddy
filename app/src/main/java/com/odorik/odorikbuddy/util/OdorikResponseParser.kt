package com.odorik.odorikbuddy.util


object OdorikResponseParser {


    fun parsePlainTextBody(body: String?): Result<String> {
        val text = body ?: ""
        if (text.startsWith("error")) {
            return Result.failure(Exception(text))
        }
        return Result.success(text)
    }
}
