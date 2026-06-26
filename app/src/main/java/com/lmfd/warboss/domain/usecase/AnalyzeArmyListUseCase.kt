package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.model.ArmyListEntry
import com.lmfd.warboss.domain.model.ListAnalysis
import com.lmfd.warboss.domain.repository.AiAnalysisRepository
import javax.inject.Inject

class AnalyzeArmyListUseCase @Inject constructor(
    private val repository: AiAnalysisRepository,
) {
    suspend operator fun invoke(
        factionName: String,
        totalPoints: Int,
        pointsLimit: Int,
        entries: List<ArmyListEntry>,
    ): ListAnalysis = repository.analyzeList(factionName, totalPoints, pointsLimit, entries)
}
