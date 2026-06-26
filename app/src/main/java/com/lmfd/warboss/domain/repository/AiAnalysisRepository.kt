package com.lmfd.warboss.domain.repository

import com.lmfd.warboss.domain.model.ArmyListEntry
import com.lmfd.warboss.domain.model.GameResult
import com.lmfd.warboss.domain.model.ListAnalysis
import com.lmfd.warboss.domain.model.MatchupAnalysis

interface AiAnalysisRepository {
    suspend fun analyzeList(
        factionName: String,
        totalPoints: Int,
        pointsLimit: Int,
        entries: List<ArmyListEntry>,
    ): ListAnalysis

    suspend fun analyzeMatchup(
        myFactionName: String,
        opponentFactionName: String,
        totalPoints: Int,
        pointsLimit: Int,
        entries: List<ArmyListEntry>,
        recentGames: List<GameResult>,
    ): MatchupAnalysis
}
