package com.lmfd.warboss.data.repository

import com.lmfd.warboss.data.ai.AiAnalysisService
import com.lmfd.warboss.domain.model.ArmyListEntry
import com.lmfd.warboss.domain.model.GameResult
import com.lmfd.warboss.domain.model.ListAnalysis
import com.lmfd.warboss.domain.model.MatchupAnalysis
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

    override suspend fun analyzeMatchup(
        myFactionName: String,
        opponentFactionName: String,
        totalPoints: Int,
        pointsLimit: Int,
        entries: List<ArmyListEntry>,
        recentGames: List<GameResult>,
    ): MatchupAnalysis = withContext(Dispatchers.IO) {
        service.analyzeMatchup(myFactionName, opponentFactionName, totalPoints, pointsLimit, entries, recentGames)
    }
}
