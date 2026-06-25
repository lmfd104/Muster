package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.model.UnitDetail
import com.lmfd.warboss.domain.repository.UnitRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class GetUnitDetailUseCase @Inject constructor(
    private val repository: UnitRepository,
) {
    suspend operator fun invoke(unitId: String): UnitDetail? =
        repository.getUnitDetail(unitId)
}
