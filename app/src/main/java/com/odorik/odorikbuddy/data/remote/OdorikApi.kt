package com.odorik.odorikbuddy.data.remote

import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.model.HistoryItem
import com.odorik.odorikbuddy.model.PublicNumber
import com.odorik.odorikbuddy.model.Route
import com.odorik.odorikbuddy.model.SharedPublicNumber
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OdorikApi {
    @GET("sms/allowed_sender")
    suspend fun getAllowedSenders(
        @Query("user") user: String,
        @Query("password") password: String
    ): Response<String>  

    @FormUrlEncoded
    @POST("sms")
    suspend fun sendSms(
        @Field("user") user: String,
        @Field("password") password: String,
        @Field("recipient") recipient: String,
        @Field("message") message: String,
        @Field("sender") sender: String? = null,
        @Field("delayed") delayed: String? = null
    ): Response<String>  

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



    @GET("calls.json")
    suspend fun getCallHistory(
        @Query("user") user: String,
        @Query("password") password: String,
        @Query("from") from: String, 
        @Query("to") to: String      
    ): List<HistoryItem>

    @GET("sms.json")
    suspend fun getSmsHistory(
        @Query("user") user: String,
        @Query("password") password: String,
        @Query("from") from: String, 
        @Query("to") to: String      
    ): List<HistoryItem>

    @GET("public_numbers.json")
    suspend fun getSharedPublicNumbers(
        @Query("user") user: String,
        @Query("password") password: String,
        @Query("include_shared_numbers") includeShared: String = "true"
    ): Response<List<SharedPublicNumber>>

    @GET("public_numbers.json")
    suspend fun getPublicNumbers(
        @Query("user") user: String,
        @Query("password") password: String
    ): Response<List<PublicNumber>>

    @GET("public_numbers/{number}/routes.json")
    suspend fun getRoutes(
        @Path("number") number: String,
        @Query("user") user: String,
        @Query("password") password: String
    ): Response<List<Route>>

    @FormUrlEncoded
    @POST("public_numbers/{number}/routes.json")
    suspend fun createRoute(
        @Path("number") number: String,
        @Field("source_number") sourceNumber: String,
        @Field("ringing_number") ringingNumber: String,
        @Query("replace_by_source_number") replaceBySource: String? = null,
        @Query("user") user: String,
        @Query("password") password: String
    ): Response<String>

    @DELETE("public_numbers/{number}/routes/{id}.json")
    suspend fun deleteRoute(
        @Path("number") number: String,
        @Path("id") id: Long,
        @Query("user") user: String,
        @Query("password") password: String
    ): Response<String>
}
