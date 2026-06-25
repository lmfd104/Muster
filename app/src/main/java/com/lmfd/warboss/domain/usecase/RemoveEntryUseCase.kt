package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.repository.ArmyListRepository
import javax.inject.Inject

class RemoveEntryUseCase @Inject constructor(
    private val repository: ArmyListRepository,
) {
    suspend operator fun invoke(entryId: String) = repository.removeEntry(entryId)
}
