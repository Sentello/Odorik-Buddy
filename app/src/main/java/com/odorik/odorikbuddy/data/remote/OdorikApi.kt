package com.odorik.odorikbuddy.data.remote

import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.data.model.LineInfo
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OdorikApi {
    @GET("sms/allowed_sender")
    suspend fun getAllowedSenders(
        @Query("user") user: String,
        @Query("password") password: String
    ): Response<String>  // Comma-separated string

    @FormUrlEncoded
    @POST("sms")
    suspend fun sendSms(
        @Field("user") user: String,
        @Field("password") password: String,
        @Field("recipient") recipient: String,
        @Field("message") message: String,
        @Field("sender") sender: String? = null,
        @Field("delayed") delayed: String? = null
    ): Response<String>  // Plain text response

    @FormUrlEncoded
    @POST("callback")
    suspend fun call(
        @Field("user") user: String,
        @Field("password") password: String,
        @Field("caller") caller: String,
        @Field("recipient") recipient: String,
        @Field("line") line: String
    ): Response<String>

    @GET("balance")
    suspend fun getCredit(
        @Query("user") user: String,
        @Query("password") password: String
    ): String

    @GET("lines.json")
    suspend fun getLines(
        @Query("user") user: String,
        @Query("password") password: String
    ): Response<List<Line>>

    @GET("line_info.json")
    suspend fun getLineInfo(
        @Query("user") user: String,
        @Query("password") password: String,
        @Query("line") line: String
    ): Response<LineInfo>
}
