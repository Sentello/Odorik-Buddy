package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.data.repository.UserRepository
import com.odorik.odorikbuddy.model.Route
import javax.inject.Inject

class GetRoutesForNumberUseCase @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {
    suspend fun execute(publicNumber: String): Result<List<Route>> {
        val userId = userRepository.getUserId()
        val password = userRepository.getPassword()

        if (userId == null || password == null) {
            return Result.failure(Exception("User not logged in"))
        }

        return try {
            val response = odorikApi.getRoutes(publicNumber, userId, password)
            if (response.isSuccessful) {
                val routes: List<Route>? = response.body()
                Result.success(routes ?: emptyList())
            } else {
                val errorMessage = response.errorBody()?.string() ?: "HTTP error: ${response.code()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}