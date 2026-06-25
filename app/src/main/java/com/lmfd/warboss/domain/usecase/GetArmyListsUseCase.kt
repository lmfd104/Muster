package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.model.ArmyList
import com.lmfd.warboss.domain.repository.ArmyListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArmyListsUseCase @Inject constructor(
    private val repository: ArmyListRepository,
) {
    operator fun invoke(): Flow<List<ArmyList>> = repository.getArmyLists()
}
