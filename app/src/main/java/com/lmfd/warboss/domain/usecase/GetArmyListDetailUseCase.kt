package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.model.ArmyList
import com.lmfd.warboss.domain.model.ArmyListEntry
import com.lmfd.warboss.domain.repository.ArmyListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetArmyListDetailUseCase @Inject constructor(
    private val repository: ArmyListRepository,
) {
    operator fun invoke(listId: String): Flow<Pair<ArmyList?, List<ArmyListEntry>>> =
        combine(
            repository.getArmyList(listId),
            repository.getArmyListEntries(listId),
        ) { list, entries -> Pair(list, entries) }
}
