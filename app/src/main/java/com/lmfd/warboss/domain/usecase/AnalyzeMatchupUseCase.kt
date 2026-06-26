package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.model.ArmyListEntry
import com.lmfd.warboss.domain.model.MatchupAnalysis
import com.lmfd.warboss.domain.repository.AiAnalysisRepository
import com.lmfd.warboss.domain.repository.GameResultRepository
import javax.inject.Inject

class AnalyzeMatchupUseCase @Inject constructor(
    private val aiRepository: AiAnalysisRepository,
    private val gameResultRepository: GameResultRepository,
) {
    suspend operator fun invoke(
        myFactionName: String,
        opponentFactionName: String,
        totalPoints: Int,
        pointsLimit: Int,
        entries: List<ArmyListEntry>,
    ): MatchupAnalysis {
        val history = gameResultRepository.getRecentByOpponent(opponentFactionName, limit = 10)
        return aiRepository.analyzeMatchup(
            myFactionName = myFactionName,
            opponentFactionName = opponentFactionName,
            totalPoints = totalPoints,
            pointsLimit = pointsLimit,
            entries = entries,
            recentGames = history,
        )
    }
}
