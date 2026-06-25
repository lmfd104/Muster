package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.model.UnitSummary
import com.lmfd.warboss.domain.repository.UnitRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class GetUnitsForFactionUseCase @Inject constructor(
    private val repository: UnitRepository,
) {
    operator fun invoke(factionId: String): Flow<List<UnitSummary>> =
        repository.getUnitsForFaction(factionId)
}
