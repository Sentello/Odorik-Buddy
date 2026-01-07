package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.api.UpdateApi
import com.odorik.odorikbuddy.model.AppUpdateInfo
import javax.inject.Inject

class UpdateRepository @Inject constructor(
    private val updateApi: UpdateApi
) {
    suspend fun getAppUpdateInfo(): Result<AppUpdateInfo> {
        return try {
            val response = updateApi.getAppUpdateInfo()
            if (response.isSuccessful) {
                response.body()?.let { 
                    Result.success(it)
                } ?: Result.failure(Exception("No data received"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}