package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.repository.RoutingRepository
import com.odorik.odorikbuddy.model.PublicNumber
import javax.inject.Inject

class GetPublicNumbersUseCase @Inject constructor(
    private val routingRepository: RoutingRepository
) {
    suspend fun execute(): Result<List<PublicNumber>> {
        return routingRepository.getPublicNumbers()
    }
}