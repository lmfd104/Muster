package com.lmfd.warboss.data.repository

import com.lmfd.warboss.data.ai.AiAnalysisService
import com.lmfd.warboss.domain.model.ArmyListEntry
import com.lmfd.warboss.domain.model.ListAnalysis
import com.lmfd.warboss.domain.repository.AiAnalysisRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiAnalysisRepositoryImpl @Inject constructor(
    private val service: AiAnalysisService,
) : AiAnalysisRepository {

    override suspend fun analyzeList(
        factionName: String,
        totalPoints: Int,
        pointsLimit: Int,
        entries: List<ArmyListEntry>,
    ): ListAnalysis = withContext(Dispatchers.IO) {
        service.analyzeList(factionName, totalPoints, pointsLimit, entries)
    }
}
