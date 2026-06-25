package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.model.Faction
import com.lmfd.warboss.domain.repository.UnitRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class GetFactionsUseCase @Inject constructor(
    private val repository: UnitRepository,
) {
    operator fun invoke(): Flow<List<Faction>> = repository.getFactions()
}
