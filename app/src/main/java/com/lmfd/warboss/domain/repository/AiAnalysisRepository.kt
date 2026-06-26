package com.lmfd.warboss.domain.repository

import com.lmfd.warboss.domain.model.ArmyListEntry
import com.lmfd.warboss.domain.model.ListAnalysis

interface AiAnalysisRepository {
    suspend fun analyzeList(
        factionName: String,
        totalPoints: Int,
        pointsLimit: Int,
        entries: List<ArmyListEntry>,
    ): ListAnalysis
}
