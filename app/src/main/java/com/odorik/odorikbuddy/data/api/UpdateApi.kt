package com.odorik.odorikbuddy.data.api

import com.odorik.odorikbuddy.model.AppUpdateInfo
import retrofit2.Response
import retrofit2.http.GET

interface UpdateApi {
    @GET("app_version.json")
    suspend fun getAppUpdateInfo(): Response<AppUpdateInfo>
}