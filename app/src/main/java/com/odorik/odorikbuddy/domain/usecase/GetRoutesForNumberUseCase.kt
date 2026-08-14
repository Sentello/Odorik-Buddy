package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.repository.RoutingRepository
import com.odorik.odorikbuddy.model.Route
import javax.inject.Inject

class GetRoutesForNumberUseCase @Inject constructor(
    private val routingRepository: RoutingRepository
) {
    suspend fun execute(publicNumber: String): Result<List<Route>> =
        routingRepository.getRoutesForNumber(publicNumber)
}
