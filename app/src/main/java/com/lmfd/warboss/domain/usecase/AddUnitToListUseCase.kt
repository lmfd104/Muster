package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.repository.ArmyListRepository
import javax.inject.Inject

class AddUnitToListUseCase @Inject constructor(
    private val repository: ArmyListRepository,
) {
    suspend operator fun invoke(listId: String, unitId: String): String =
        repository.addUnitToList(listId, unitId)
}
