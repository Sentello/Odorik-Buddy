package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.repository.RoutingRepository
import javax.inject.Inject

class CreateRouteUseCase @Inject constructor(
    private val routingRepository: RoutingRepository
) {
    suspend fun execute(
        publicNumber: String,
        sourceNumber: String,
        ringingNumber: String,
        replaceBySource: Boolean,
        useCallerIdPrefix: Boolean
    ): Result<String> {
        val finalRingingNumber = if (useCallerIdPrefix) "*087$ringingNumber" else ringingNumber
        return routingRepository.createRoute(publicNumber, sourceNumber, finalRingingNumber, replaceBySource)
    }

    suspend fun executeWithLineCredentials(
        publicNumber: String,
        sourceNumber: String,
        ringingNumber: String,
        replaceBySource: Boolean,
        useCallerIdPrefix: Boolean,
        lineId: String,
        sipPassword: String
    ): Result<String> {
        val finalRingingNumber = if (useCallerIdPrefix) "*087$ringingNumber" else ringingNumber
        return routingRepository.createRouteWithLineCredentials(
            publicNumber,
            sourceNumber,
            finalRingingNumber,
            replaceBySource,
            lineId,
            sipPassword
        )
    }
}