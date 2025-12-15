package com.odorik.odorikbuddy.domain.usecase

import com.odorik.odorikbuddy.data.repository.RoutingRepository
import com.odorik.odorikbuddy.model.SharedPublicNumber
import javax.inject.Inject

class GetSharedPublicNumbersUseCase @Inject constructor(
    private val routingRepository: RoutingRepository
) {
    suspend fun execute(): Result<List<SharedPublicNumber>> {
        return routingRepository.getSharedPublicNumbers()
    }
}