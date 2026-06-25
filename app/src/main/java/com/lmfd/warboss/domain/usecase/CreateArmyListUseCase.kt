package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.repository.ArmyListRepository
import javax.inject.Inject

class CreateArmyListUseCase @Inject constructor(
    private val repository: ArmyListRepository,
) {
    suspend operator fun invoke(
        name: String,
        factionId: String,
        factionName: String,
        pointsLimit: Int = 0,
    ): String = repository.createArmyList(name, factionId, factionName, pointsLimit)
}
