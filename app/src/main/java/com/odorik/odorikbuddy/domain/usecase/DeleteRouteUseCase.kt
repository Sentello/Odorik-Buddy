package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.repository.RoutingRepository
import javax.inject.Inject

class DeleteRouteUseCase @Inject constructor(
    private val routingRepository: RoutingRepository
) {
    suspend fun execute(publicNumber: String, routeId: Long): Result<String> =
        routingRepository.deleteRoute(publicNumber, routeId)
}
