package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject

class CreateRouteUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun execute(
        publicNumber: String,
        sourceNumber: String,
        ringingNumber: String,
        replaceBySource: Boolean? = null,
        useCallerIdPrefix: Boolean? = null
    ): Result<String> {
        val userId = userRepository.getUserId()
        val password = userRepository.getPassword()

        if (userId == null || password == null) {
            return Result.failure(Exception("User not logged in"))
        }

        return try {
            val finalRingingNumber = if (useCallerIdPrefix == true) "*087$ringingNumber" else ringingNumber
            val replaceParam = replaceBySource?.let { if (it) "true" else null }
            val response = odorikApi.createRoute(
                publicNumber,
                sourceNumber,
                finalRingingNumber,
                replaceParam,
                userId,
                password
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: "Route created successfully")
            } else {
                val errorMessage = response.errorBody()?.string() ?: "HTTP error: ${response.code()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}