package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.remote.OdorikApi
// import com.odorik.odorikbuddy.data.model.RpcRequest // Commented out
import com.odorik.odorikbuddy.data.model.SmsInfo
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject
// import com.google.gson.Gson // Commented out
// import com.google.gson.reflect.TypeToken // Commented out

class GetSmsListUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    // Commented out the execute method for now
    /*
    suspend fun execute(): Result<List<SmsInfo>> {
        val userId = userRepository.getUserId()
        val password = userRepository.getPassword()

        if (userId == null || password == null) {
            return Result.failure(Exception("User not logged in"))
        }

        return try {
            val response = odorikApi.getSmsList(RpcRequest("get_sms_list", listOf(userId, password)))
            if (response.result != null) {
                // Convert JsonElement to List<SmsInfo> using Gson and TypeToken
                val gson = Gson()
                val listType = object : TypeToken<List<SmsInfo>>() {}.type
                val smsList = gson.fromJson<List<SmsInfo>>(response.result, listType)
                Result.success(smsList)
            } else {
                Result.failure(Exception(response.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    */
}