package com.odorik.odorikbuddy.data.repository

import com.odorik.odorikbuddy.data.remote.OdorikApi
import com.odorik.odorikbuddy.model.PublicNumber
import com.odorik.odorikbuddy.model.Route
import com.odorik.odorikbuddy.model.SharedPublicNumber
import javax.inject.Inject



class RoutingRepository @Inject constructor(
    private val odorikApi: OdorikApi,
    private val userRepository: UserRepository
) {



    suspend fun getSharedPublicNumbers(): Result<List<SharedPublicNumber>> {
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = odorikApi.getSharedPublicNumbers(userId, password)
            if (response.isSuccessful) {
                val sharedNumbers = response.body()?.filter { it.type == "shared" } ?: emptyList()
                Result.success(sharedNumbers)
            } else {
                Result.failure(Exception(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPublicNumbers(): Result<List<PublicNumber>> {
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = odorikApi.getPublicNumbers(userId, password)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoutesForNumber(number: String): Result<List<Route>> {
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = odorikApi.getRoutes(number, userId, password)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRoute(
        publicNumber: String,
        sourceNumber: String,
        ringingNumber: String,
        replaceBySource: Boolean
    ): Result<String> {
        return try {
            val (userId, userPassword) = userRepository.requireCredentials()
            val response = odorikApi.createRoute(
                number = publicNumber,
                sourceNumber = sourceNumber,
                ringingNumber = ringingNumber,
                replaceBySource = if (replaceBySource) "true" else null,
                user = userId,
                password = userPassword
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: "")
            } else {
                Result.failure(Exception(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRouteWithLineCredentials(
        publicNumber: String,
        sourceNumber: String,
        ringingNumber: String,
        replaceBySource: Boolean,
        lineId: String,
        sipPassword: String
    ): Result<String> {
        return try {
            val response = odorikApi.createRouteWithLineCredentials(
                number = publicNumber,
                sourceNumber = sourceNumber,
                ringingNumber = ringingNumber,
                replaceBySource = if (replaceBySource) "true" else null,
                user = lineId,
                password = sipPassword
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: "")
            } else {
                Result.failure(Exception(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRoute(publicNumber: String, routeId: Long): Result<String> {
        return try {
            val (userId, password) = userRepository.requireCredentials()
            val response = odorikApi.deleteRoute(publicNumber, routeId, userId, password)
            if (response.isSuccessful) {
                Result.success(response.body() ?: "")
            } else {
                Result.failure(Exception(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}