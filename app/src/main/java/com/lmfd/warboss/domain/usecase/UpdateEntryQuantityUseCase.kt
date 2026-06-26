package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.repository.ArmyListRepository
import javax.inject.Inject

class UpdateEntryQuantityUseCase @Inject constructor(
    private val repository: ArmyListRepository,
) {
    suspend operator fun invoke(entryId: String, quantity: Int) {
        if (quantity >= 1) repository.updateEntryQuantity(entryId, quantity)
    }
}
