package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import javax.inject.Inject

class DeleteRouteUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun execute(publicNumber: String, routeId: Long): Result<String> {
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = odorikApi.deleteRoute(publicNumber, routeId, userId, password)
            if (response.isSuccessful) {
                Result.success(response.body() ?: "Route deleted successfully")
            } else {
                val errorMessage = response.errorBody()?.string() ?: "HTTP error: ${response.code()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}