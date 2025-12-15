package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.remote.OdorikApi
// import com.odorik.odorikbuddy.data.model.RpcRequest // Commented out
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject
// import com.google.gson.Gson // Commented out

class SendSmsUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    // Commented out the execute method for now
    /*
    suspend fun execute(recipient: String, message: String): Result<String> {
        val userId = userRepository.getUserId()
        val password = userRepository.getPassword()

        if (userId == null || password == null) {
            return Result.failure(Exception("User not logged in"))
        }

        return try {
            val response = odorikApi.sendSms(RpcRequest("send_sms", listOf(userId, password, recipient, message)))
            if (response.result != null) {
                // Convert JsonElement to String
                Result.success(response.result.asString) // Assuming the result is a simple string
            } else {
                Result.failure(Exception(response.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    */
}